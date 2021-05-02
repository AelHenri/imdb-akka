package com.canal.models

final case class Principal(
    name: String,
    birthYear: Option[Int],
    deathYear: Option[Int],
    profession: Option[List[String]]
)

object Principal {
    def fromPerson(person: Person) = Principal(
        person.name,
        person.birthYear,
        person.deathYear,
        person.profession
    )
}