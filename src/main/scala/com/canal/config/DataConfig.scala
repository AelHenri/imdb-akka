package com.canal.config

import com.typesafe.config.ConfigFactory

object DataConfig {
    private val config = ConfigFactory.load()
    private val DATA_FOLDER = config.getString("input-data.files.dataFolder")

    private def getFilePath(s: String) = {
        sys.env.get("IMDB_DATA_FOLDER").getOrElse(DATA_FOLDER) + config.getString(s)
    }

    val TITLES_FILE = getFilePath("input-data.files.titles")
    val PRINCIPALS_FILE = getFilePath("input-data.files.principals")
    val NAMES_FILE = getFilePath("input-data.files.names")
    val EPISODES_FILE = getFilePath("input-data.files.episodes")
    
    val TITLES_ID = config.getString("input-data.columns.titles.id")
    val TITLES_PRIMARY = config.getString("input-data.columns.titles.primary")
    val TITLES_ORIGINAL = config.getString("input-data.columns.titles.original")
    val TITLES_TYPE = config.getString("input-data.columns.titles.type")
    val TITLES_STARTYEAR = config.getString("input-data.columns.titles.startYear")
    val TITLES_ENDYEAR = config.getString("input-data.columns.titles.endYear")
    val TITLES_GENRES = config.getString("input-data.columns.titles.genres")

    val PRINCIPALS_NID = config.getString("input-data.columns.principals.nameId")
    val PRINCIPALS_TID = config.getString("input-data.columns.principals.titleId")

    val NAMES_ID = config.getString("input-data.columns.names.id")
    val NAMES_NAME = config.getString("input-data.columns.names.name")
    val NAMES_BIRTHYEAR = config.getString("input-data.columns.names.birthYear")
    val NAMES_DEATHYEAR = config.getString("input-data.columns.names.deathYear")
    val NAMES_PROFESSION = config.getString("input-data.columns.names.profession")

    val EPISODES_ID = config.getString("input-data.columns.episodes.id")
    val EPISODES_PARENTID = config.getString("input-data.columns.episodes.parentId")
    val EPISODES_SEASONNUM = config.getString("input-data.columns.episodes.seasonNumber")
    val EPISODES_EPISODENUM = config.getString("input-data.columns.episodes.episodeNumber")

    val FILTER_MOVIE = config.getString("input-data.filters.movie")
    val FILTER_SERIES = config.getString("input-data.filters.series")
}