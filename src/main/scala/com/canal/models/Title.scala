package com.canal.models
import com.canal.config.DataConfig._

final case class Title(
    id: String,
    titleType: String,
    primary: String,
    original: String,
    startYear: Option[Int],
    endYear: Option[Int],
    genres: Option[List[String]]
)