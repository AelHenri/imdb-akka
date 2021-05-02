package com.canal

import akka.stream._
import akka.stream.scaladsl._
import akka.stream.alpakka.csv.scaladsl.{CsvParsing, CsvToMap}
import java.nio.file.Paths
import com.canal.config.DataConfig._
import com.canal.models._

object ImdbTsvParser {

    private val delimiter: Byte = '\t'
    private val quoteChar: Byte = 'ˆ'.toByte
    private val escapeChar: Byte = '|'
    private val csvLineScanner = CsvParsing.lineScanner(delimiter=delimiter, quoteChar=quoteChar, escapeChar=escapeChar)

    def streamFile(path: String): Source[Map[String, String], _] = {
        FileIO.fromPath(Paths.get(path))
            .via(csvLineScanner)
            .via(CsvToMap.toMapAsStrings())
    }

    def mapToPerson(personMap: Map[String, String]): Person = {
        Person(
            id = personMap(NAMES_ID),
            name = personMap(NAMES_NAME),
            birthYear = personMap(NAMES_BIRTHYEAR).toIntOption,
            deathYear = personMap(NAMES_DEATHYEAR).toIntOption,
            profession = personMap.get(NAMES_PROFESSION).map(_.split(",").toList)
        )
    }

    def mapToTitle(titleMap: Map[String, String]): Title = {
        Title(
            id = titleMap(TITLES_ID),
            titleType = titleMap(TITLES_TYPE),
            primary = titleMap(TITLES_PRIMARY),
            original = titleMap(TITLES_ORIGINAL),
            startYear = titleMap(TITLES_STARTYEAR).toIntOption,
            endYear = titleMap(TITLES_ENDYEAR).toIntOption,
            genres = titleMap.get(TITLES_GENRES).map(_.split(",").toList)
        )
    }

    def mapToPrincipalTitleLink(principalTitleMap: Map[String, String]): PrincipalTitleLink = {
        PrincipalTitleLink(
            principalTitleMap(PRINCIPALS_NID),
            principalTitleMap(PRINCIPALS_TID)
        )
    }

    def mapToEpisode(episodeMap: Map[String, String]): Episode = {
        Episode(
            episodeId = episodeMap(EPISODES_ID),
            parentId = episodeMap(EPISODES_PARENTID),
            seasonNumber = episodeMap(EPISODES_SEASONNUM).toIntOption,
            episodeNumber = episodeMap(EPISODES_EPISODENUM).toIntOption
        )
    }
}