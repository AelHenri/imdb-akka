package com.canal.models

final case class Person(
    id: String,
    name: String,
    birthYear: Option[Int],
    deathYear: Option[Int],
    profession: Option[List[String]]
)