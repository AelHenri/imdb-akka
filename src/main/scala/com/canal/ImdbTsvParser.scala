package com.canal

import akka.stream._
import akka.stream.scaladsl._
import akka.stream.alpakka.csv.scaladsl.{CsvParsing, CsvToMap}
import java.nio.file.Paths
import com.canal.config.DataConfig._
import com.canal.models._

object ImdbTsvParser {

    private val delimiter: Byte = '\t'
    private val quoteChar: Byte = '%'
    private val escapeChar: Byte = '|'
    private val csvLineScanner = CsvParsing.lineScanner(delimiter=delimiter, quoteChar=quoteChar, escapeChar=escapeChar)

    def streamFile(path: String): Source[Map[String, String], _] = {
        FileIO.fromPath(Paths.get(path))
            .via(csvLineScanner)
            .via(CsvToMap.toMapAsStrings())
    }

    def mapToPrincipal(principalMap: Map[String, String]): Principal = {
        Principal(
            name = principalMap(NAMES_NAME),
            birthYear = principalMap(NAMES_BIRTHYEAR).toInt,
            deathYear = principalMap(NAMES_DEATHYEAR).toIntOption,
            profession = principalMap(NAMES_PROFESSION).split(",").toList
        )
    }

    def mapToTitle(tvSeriesMap: Map[String, String]): Title = {
        Title(
            original = tvSeriesMap(TITLES_TITLE),
            startYear = tvSeriesMap(TITLES_STARTYEAR).toInt,
            endYear = tvSeriesMap(TITLES_ENDYEAR).toIntOption,
            genres = tvSeriesMap(TITLES_GENRES).split(",").toList
        )
    }

    def mapToPrincipalTitleLink(principalTitleMap: Map[String, String]): PrincipalTitleLink = {
        PrincipalTitleLink(
            principalTitleMap(PRINCIPALS_TID),
            principalTitleMap(PRINCIPALS_NID)
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