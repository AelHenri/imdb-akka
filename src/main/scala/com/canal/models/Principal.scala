package com.canal.models
import com.canal.config.DataConfig._

final case class Principal(
    name: String,
    birthYear: Int,
    deathYear: Option[Int],
    profession: List[String]
)