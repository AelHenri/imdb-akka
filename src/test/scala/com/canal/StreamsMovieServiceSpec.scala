package com.canal

import org.scalatest._
import flatspec._
import matchers._
import akka.stream._
import akka.stream.scaladsl._
import akka.stream.scaladsl.Sink
import akka.actor.ActorSystem
import scala.concurrent.ExecutionContext
import scala.concurrent.Await
import scala.concurrent.duration._
import models._

class StreamsMovieServiceSpec extends AnyFlatSpec with should.Matchers {

    implicit val system = ActorSystem("MovieService")
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    implicit val ec: ExecutionContext = ExecutionContext.global

    def fixture = 
        new {
            val movieService = new StreamsMovieService()
        }

    "A Streams Movie Service" should "return the principals from Pulp Fiction" in {
        val f = fixture

        val futurePrincipals = f.movieService.principalsForMovieName("Pulp Fiction")
            .runWith(Sink.seq)
        
        val results: Seq[Principal] = Await.result(futurePrincipals, 3.seconds)

        val expected: Seq[Principal] = Seq(
            Principal("Quentin Tarantino", 1963, None, List("writer", "actor", "producer")),
            Principal("Uma Thurman",1970,None,List("actress", "soundtrack", "producer")),
            Principal("John Travolta",1954,None,List("actor", "soundtrack", "producer")),
            Principal("Lawrence Bender",1957,None,List("producer", "camera_department", "actor")),
            Principal("Samuel L. Jackson",1948,None,List("actor", "producer", "soundtrack")),
            Principal("Tim Roth",1961,None,List("actor", "producer", "soundtrack"))
        )

        results should contain theSameElementsAs (expected)
    }

    it should "return an empty sequence of principals from an inexistant film name" in {
        val f = fixture

        val futurePrincipals = f.movieService.principalsForMovieName("Star Wars")
            .runWith(Sink.seq)
        val results: Seq[Principal] = Await.result(futurePrincipals, 3.seconds)

        results shouldBe empty
    }

    it should "return the 3 series with the most episodes" in {
        val f = fixture

        val futureTopThreeSeries = f.movieService.topTvSeriesWithGreatestNumberOfEpisodes(3)
            .runWith(Sink.seq)
        val results = Await.result(futureTopThreeSeries, 3.seconds)

        val expected = Seq(
            Title("Doctor Who", 2005, None, List("Adventure", "Drama", "Family")),
            Title("Lost", 2004, Some(2010), List("Adventure", "Drama", "Fantasy")),
            Title("Shin seiki evangerion", 1995, Some(1996), List("Action", "Animation", "Drama"))
        )

        results should contain theSameElementsAs (expected)
    }

    it should "return an empty sequence of series when asking for no series" in {
        val f = fixture

        val futureEmptySeries = f.movieService.topTvSeriesWithGreatestNumberOfEpisodes(0)
            .runWith(Sink.seq)
        val results = Await.result(futureEmptySeries, 3.seconds)

        results shouldBe empty
    }

    it should "return the max number of series whem asked for more than contained in the file" in {
        val f = fixture

        val futureEmptySeries = f.movieService.topTvSeriesWithGreatestNumberOfEpisodes(10)
            .runWith(Sink.seq)
        val results = Await.result(futureEmptySeries, 3.seconds)

        results.length should be (8)
    }
}