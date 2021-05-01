package com.canal

import akka.stream._
import akka.stream.scaladsl._
import akka.actor.ActorSystem
import scala.concurrent.ExecutionContext
import scala.concurrent.Await
import scala.concurrent.duration._

object Main extends App {
    implicit val system = ActorSystem("MovieService")
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    implicit val ec: ExecutionContext = ExecutionContext.global

    val movieService = new StreamsMovieService()

    val starWarsPrincipals = movieService.principalsForMovieName("Star Wars")
    val pulpFictionPrincipals = movieService.principalsForMovieName("Pulp Fiction")
    val topSeries = movieService.tvSeriesWithGreatestNumberOfEpisodes()

    println("Series with the most episodes:")
    topSeries.runForeach(println)

    starWarsPrincipals.runForeach(p => println("Star Wars: " + p))
    pulpFictionPrincipals.runForeach(println)
}