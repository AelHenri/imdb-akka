package com.canal.db

import akka.stream._
import akka.stream.scaladsl._
import akka.actor.ActorSystem
import scala.concurrent.ExecutionContext
import akka.stream.ActorMaterializer
import akka.stream.alpakka.slick.scaladsl._
import slick.jdbc.GetResult
import scala.concurrent.Await
import scala.concurrent.duration._
import com.canal.ImdbTsvParser
import com.canal.config.DataConfig._
import com.canal.config.DbConfig._
import com.canal.models._
import scala.concurrent.Future
import akka.Done
import scala.util.Success
import scala.util.Failure
import akka.event.Logging

object DatabaseInitializer extends App {
    implicit val system = ActorSystem("MovieService")
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    implicit val ec: ExecutionContext = ExecutionContext.global
    val profile = slick.jdbc.SQLiteProfile
    import profile.api._
    val db = Database.forConfig("slick-sqlite.db")
    implicit val slickSession = SlickSession.forDbAndProfile(db, profile)

    private def streamFileToRecord(path: String, destination: String) = {
        ImdbTsvParser
            .streamFile(path)
            .map(Record(destination, _))
            .grouped(GROUP_SIZE)
    }

    val setupDbFuture = db.run(Queries.createSchemaIfNotExists)
    Await.result(setupDbFuture, 1.seconds)
    system.registerOnTermination(() => slickSession.close())

    val pragmaJournaling = db.run(Queries.pragmaJournalingQuery)
    Await.result(pragmaJournaling, 1.seconds)

    val titlesSource = streamFileToRecord(TITLES_FILE, "title")
    val peopleSource = streamFileToRecord(NAMES_FILE, "person")
    val principalsSource = streamFileToRecord(PRINCIPALS_FILE, "principal")
    val episodesSource = streamFileToRecord(EPISODES_FILE, "episode")
    
    val combinedSource = Source.combine(titlesSource, peopleSource, principalsSource, episodesSource)(Merge(_))

    val done: Future[Done] = combinedSource
        .map(Queries.recordsToTransaction(_))
        .mapAsync(PARALLELISM)(db.run(_))
        .log("after insert")
        .withAttributes(Attributes.logLevels(
            onElement = Logging.ErrorLevel,
            onFinish = Logging.InfoLevel,
            onFailure = Logging.InfoLevel
        ))
        .runWith(Sink.ignore)
    
    done.onComplete {
        case Success(done) => {
            println(done)
            system.terminate()

        }
        case Failure(e) => {
            e.printStackTrace(System.out)
            system.terminate()
        }
    }
}