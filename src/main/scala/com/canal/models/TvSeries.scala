package com.canal.models

final case class TvSeries(
    original: String,
    startYear: Option[Int],
    endYear: Option[Int],
    genres: Option[List[String]]
)

object TvSeries {
    def fromTitle(title: Title) = TvSeries(
        title.original,
        title.startYear,
        title.endYear,
        title.genres
    )
}