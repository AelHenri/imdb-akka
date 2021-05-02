package com.canal

import akka.stream._
import akka.stream.scaladsl._
import akka.actor.ActorSystem
import scala.concurrent.ExecutionContext
import scala.concurrent.Await
import scala.concurrent.duration._
import com.canal.services._
import scala.concurrent.Future
import akka.Done
import scala.util.Success
import scala.util.Failure
import akka.stream.alpakka.slick.javadsl.SlickSession

object Main extends App {

    implicit val system = ActorSystem("MovieService")
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    implicit val ec: ExecutionContext = ExecutionContext.global

    final case class Arguments (
        command: String,
        movie: Option[String],
        streams: Boolean
    )

    private val usage = """
        |Usage: 
        |   - sbt run topTvSeries [--streams]
        |   - sbt run principalsFromMovie [--streams] movieName
        |   - sbt run help
    """.stripMargin

    def parseArguments(argsList: List[String], parsedArgs: Arguments): Arguments = {
        argsList match {
            case Nil => parsedArgs
            case ("help" | "--help") :: Nil => Arguments("help", None, false)
            case "--streams" :: tail => parseArguments(tail, Arguments(parsedArgs.command, parsedArgs.movie, true))
            case "principalsFromMovie" :: value :: tail if (parsedArgs.command == "help") =>
                parseArguments(tail, Arguments("principalsFromMovie", Some(value), parsedArgs.streams))
            case "topTvSeries" :: tail if (parsedArgs.command == "help") => parseArguments(tail, Arguments("topTvSeries", None, parsedArgs.streams))
            case _ => Arguments("help", None, false)
        }
    }

    def printTopTvSeries(movieService: MovieService): Future[Done] = {
        val topTvSeriesSource = movieService.tvSeriesWithGreatestNumberOfEpisodes()

        println("Top 10 Tv Series with the most episodes of all times:")
        topTvSeriesSource.runForeach(println)
    }

    def printPrincipalsForMovie(movieService: MovieService, movieName: String): Future[Done] = {
        val principalsFromMovie = movieService.principalsForMovieName(movieName)

        println(s"Principals from $movieName:")
        principalsFromMovie.runForeach(println)
    }

    def printUsage(): Future[Done] = {
        Source.single(usage).runForeach(println)
    }

    val parsedArgs = parseArguments(args.toList, Arguments("help", None, false))
    val movieService = if (parsedArgs.streams) new StreamsMovieService() else {
        implicit val session = SlickSession.forConfig("slick-sqlite")
        system.registerOnTermination(() => session.close())
        new SqlMovieService()
    }
    val futureResult = parsedArgs match {
        case Arguments("help", _, _) => printUsage()
        case Arguments("topTvSeries", _, _) => printTopTvSeries(movieService)
        case Arguments("principalsFromMovie", Some(movie), _) =>  printPrincipalsForMovie(movieService, movie)
        case _ => printUsage()
    }

    futureResult.onComplete {
        case Success(value) => {
            println(value)
            system.terminate()
        }
        case Failure(exception) => {
            exception.printStackTrace(System.out)
            system.terminate()
        }
    }
}