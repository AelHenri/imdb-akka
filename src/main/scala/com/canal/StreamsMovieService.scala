package com.canal

import akka.stream._
import akka.stream.scaladsl._
import akka.actor.ActorSystem
import akka.stream.alpakka.csv.scaladsl.{CsvParsing, CsvToMap}
import java.nio.file.Paths
import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import scala.util.Success
import scala.util.Failure
import com.canal.config.DataConfig._
import com.canal.models._

class StreamsMovieService(implicit system: ActorSystem, materializer: ActorMaterializer, ec: ExecutionContext) extends MovieService{

    private def filterOnFutureOption[T](valueToCheck: T, futureFilter: Future[Option[T]]): Future[Boolean] = {
        futureFilter.map(_ match {
            case Some(x) => x == valueToCheck
            case None => false
        })
    }

    private def filterOnFutureSeq[T](valueToCheck: T, futureSeqOfFilters: Future[Seq[T]]): Future[Boolean] = {
        futureSeqOfFilters.map(_.contains(valueToCheck))
    }

    private def asyncFilter[T](filter: T => Future[Boolean], parallelism: Int=1): Flow[T, T, _] = {
        Flow[T].mapAsync(1)(p => filter(p).map(_ -> p))
               .filter(_._1)
               .map(_._2)
    }

    def principalsForMovieName(name: String): Source[Principal, _] = {
        val titles = ImdbTsvParser.streamFile(TITLES_FILE)
        val principals = ImdbTsvParser.streamFile(PRINCIPALS_FILE)
        val names = ImdbTsvParser.streamFile(NAMES_FILE)
        
        val movieId = titles
            .filter(t => t(TITLES_TYPE) == FILTER_MOVIE)
            .filter(t => t(TITLES_TITLE) == name)
            .map(t => t(TITLES_ID))
            .runWith(Sink.headOption)
        
        val principalIds = principals
            .via(asyncFilter(p => filterOnFutureOption(p(PRINCIPALS_TID), movieId)))
            .map(p => p(PRINCIPALS_NID))
            .runWith(Sink.seq)
        
        names
            .via(asyncFilter(n => filterOnFutureSeq(n(NAMES_ID), principalIds)))
            .map(ImdbTsvParser.mapToPrincipal(_))
    }

    def topTvSeriesWithGreatestNumberOfEpisodes(topNumber: Int): Source[Title, _] = {
        val titles = ImdbTsvParser.streamFile(TITLES_FILE)
        val episodes = ImdbTsvParser.streamFile(EPISODES_FILE)

        val episodeCounts = episodes
            .map(_(EPISODES_PARENTID))
            .groupBy(Integer.MAX_VALUE, identity)
            .map(_ -> 1)
            .reduce((l, r) => (l._1, l._2 + r._2))
            .mergeSubstreams
            .runWith(Sink.seq)
        
        val topTenIds = episodeCounts.map(counts => counts
            .sortBy(_._2)(Ordering[Int].reverse)
            .take(topNumber)
            .map(_._1)
        )

        titles
            .filter(t => t(TITLES_TYPE) == FILTER_SERIES)
            .via(asyncFilter(t => filterOnFutureSeq(t(TITLES_ID), topTenIds)))
            .map(ImdbTsvParser.mapToTitle(_))
    }

    def tvSeriesWithGreatestNumberOfEpisodes(): Source[Title, _] = {
        topTvSeriesWithGreatestNumberOfEpisodes(10)
    }
}
