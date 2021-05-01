package com.canal.models
import com.canal.config.DataConfig._

final case class Title(
    original: String,
    startYear: Int,
    endYear: Option[Int],
    genres: List[String]
)