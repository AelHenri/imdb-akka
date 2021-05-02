package com.canal.services

import akka.stream.scaladsl.Source
import com.canal.models._

trait MovieService {
    def principalsForMovieName(name: String): Source[Principal, _]
    def tvSeriesWithGreatestNumberOfEpisodes(): Source[TvSeries, _]
}