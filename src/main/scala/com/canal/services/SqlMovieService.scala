package com.canal.services

import com.canal.models._
import akka.stream._
import akka.stream.scaladsl.Source
import akka.stream.alpakka.slick.javadsl.SlickSession
import akka.actor.ActorSystem
import scala.concurrent.ExecutionContext
import slick.jdbc.GetResult
import akka.stream.alpakka.slick.scaladsl.Slick
import slick.driver.SQLiteDriver.api._

class SqlMovieService(implicit system: ActorSystem, materializer: ActorMaterializer, ec: ExecutionContext, session: SlickSession) extends MovieService{

    implicit val getPersonResult = GetResult(r => 
        Person(r.nextString, r.nextString, r.nextIntOption, r.nextIntOption, r.nextStringOption.map(_.split(",").toList))
    )

    implicit val getTitleResult = GetResult(r =>
        Title(r.nextString, r.nextString, r.nextString, r.nextString, r.nextIntOption, r.nextIntOption, r.nextStringOption.map(_.split(",").toList))
    )

    def principalsForMovieName(name: String): Source[Principal, _] = {
        Slick.source(sql"""
            SELECT p.id, full_name, birth_year, death_year, profession
            FROM titles t
            LEFT JOIN principal_title_links ptl ON t.id = ptl.title_id
            LEFT JOIN people p ON ptl.principal_id = p.id
            WHERE (t.primary_title = $name OR t.original_title = $name) AND t.title_type = 'movie'
        """.as[Person])
            .map(Principal.fromPerson(_))
    }
    
    def topTvSeriesWithGreatestNumberOfEpisodes(top: Int): Source[TvSeries, _] = {
        Slick.source(sql"""
            SELECT t.id, title_type, primary_title, original_title, start_year, end_year, genres
            FROM titles t
            LEFT JOIN episodes e ON t.id = e.parent_id
            GROUP BY t.id
            ORDER BY COUNT(*) DESC
            LIMIT $top
        """.as[Title])
            .map(TvSeries.fromTitle(_))
    }

    def tvSeriesWithGreatestNumberOfEpisodes(): Source[TvSeries, _] = {
        topTvSeriesWithGreatestNumberOfEpisodes(10)
    }
    
}