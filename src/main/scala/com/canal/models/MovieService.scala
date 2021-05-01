package com.canal.models

import akka.stream.scaladsl.Source

trait MovieService {
    def principalsForMovieName(name: String): Source[Principal, _]
    def tvSeriesWithGreatestNumberOfEpisodes(): Source[Title, _]
}