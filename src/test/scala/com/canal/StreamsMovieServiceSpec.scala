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
import com.canal.services._

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
            Principal("Quentin Tarantino", Some(1963), None, Some(List("writer", "actor", "producer"))),
            Principal("Uma Thurman",Some(1970),None,Some(List("actress", "soundtrack", "producer"))),
            Principal("John Travolta",Some(1954),None,Some(List("actor", "soundtrack", "producer"))),
            Principal("Lawrence Bender",Some(1957),None,Some(List("producer", "camera_department", "actor"))),
            Principal("Samuel L. Jackson",Some(1948),None,Some(List("actor", "producer", "soundtrack"))),
            Principal("Tim Roth",Some(1961),None,Some(List("actor", "producer", "soundtrack")))
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
            TvSeries("Doctor Who", Some(2005), None, Some(List("Adventure", "Drama", "Family"))),
            TvSeries("Lost", Some(2004), Some(2010), Some(List("Adventure", "Drama", "Fantasy"))),
            TvSeries("Shin seiki evangerion", Some(1995), Some(1996), Some(List("Action", "Animation", "Drama")))
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