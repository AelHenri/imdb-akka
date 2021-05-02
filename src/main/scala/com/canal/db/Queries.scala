package com.canal.db
import slick.driver.SQLiteDriver.api._
import com.canal.models._
import com.canal.ImdbTsvParser

object Queries {

    private def createTitles: DBIO[Int] =
        sqlu"""CREATE TABLE IF NOT EXISTS titles (
            id TEXT PRIMARY KEY,
            title_type TEXT NOT NULL,
            primary_title TEXT NOT NULL,
            original_title TEXT NOT NULL,
            start_year INTEGER,
            end_year INTEGER,
            genres TEXT
        )"""
    
    private def createPeople: DBIO[Int] = 
        sqlu"""CREATE TABLE IF NOT EXISTS people (
            id TEXT PRIMARY KEY,
            full_name TEXT NOT NULL,
            birth_year INTEGER,
            death_year INTEGER,
            profession TEXT
        )"""
    
    private def createLinks: DBIO[Int] = 
        sqlu"""CREATE TABLE IF NOT EXISTS principal_title_links (
            principal_id TEXT NOT NULL,
            title_id TEXT NOT NULL,
            PRIMARY KEY (principal_id, title_id)
        )"""
    
    private def createEpisodes: DBIO[Int] =
        sqlu"""CREATE TABLE IF NOT EXISTS episodes (
            id TEXT PRIMARY KEY,
            parent_id TEXT NOT NULL,
            season_number INTEGER,
            episode_number INTEGER
        )"""
    
    private def createPrimaryTitleIndex: DBIO[Int] = 
        sqlu"""CREATE INDEX IF NOT EXISTS titles_primary_index ON titles(primary_title)"""
    
    private def createOriginalTitleIndex: DBIO[Int] = 
        sqlu"""CREATE INDEX IF NOT EXISTS titles_original_index ON titles(original_title)"""
    
    private def createEpisodesParentIndex: DBIO[Int] = 
        sqlu"""CREATE INDEX IF NOT EXISTS episodes_parent_index ON episodes(parent_id)"""

    def pragmaJournalingQuery = sql"""PRAGMA journal_mode=WAL""".as[String]
    def pragmaSynchronousQuery = sqlu"""PRAGMA synchronous=OFF"""
    
    def createSchemaIfNotExists: DBIO[Unit] = DBIO.seq(
        pragmaSynchronousQuery,
        createTitles,
        createPeople,
        createLinks,
        createEpisodes,
        createPrimaryTitleIndex,
        createOriginalTitleIndex,
        createEpisodesParentIndex
    )

    def insertTitleQuery(title: Title) =
        sqlu"""INSERT OR REPLACE INTO titles(id, title_type, primary_title, original_title, start_year, end_year, genres)
        VALUES(${title.id}, ${title.titleType}, ${title.primary}, ${title.original}, ${title.startYear},
            ${title.startYear}, ${title.genres.map(_.mkString(","))})
        """
    
    def insertPersonQuery(person: Person) =
        sqlu"""INSERT OR REPLACE INTO people(id, full_name, birth_year, death_year, profession)
        VALUES(${person.id}, ${person.name}, ${person.birthYear}, ${person.deathYear}, ${person.profession.map(_.mkString(","))})
        """
    
    def insertPrincipalTitleLinkQuery(principalTitleLink: PrincipalTitleLink) =
        sqlu"""INSERT OR REPLACE INTO principal_title_links(principal_id, title_id)
        VALUES(${principalTitleLink.principalId}, ${principalTitleLink.titleId})
        """
    
    def insertEpisodeQuery(episode: Episode) =
        sqlu"""INSERT OR REPLACE INTO episodes(id, parent_id, season_number, episode_number)
        VALUES(${episode.episodeId}, ${episode.parentId}, ${episode.seasonNumber}, ${episode.episodeNumber})
        """

    def recordToInsertQuery(record: Record) = record match {
        case Record("title", content) => Queries.insertTitleQuery(ImdbTsvParser.mapToTitle(content))
        case Record("person", content) => Queries.insertPersonQuery(ImdbTsvParser.mapToPerson(content))
        case Record("principal", content) => Queries.insertPrincipalTitleLinkQuery(ImdbTsvParser.mapToPrincipalTitleLink(content))
        case Record("episode", content) => Queries.insertEpisodeQuery(ImdbTsvParser.mapToEpisode(content))
        case _ => sqlu""""""
    }

    def recordsToTransaction(records: Seq[Record]): DBIO[Unit]= {
        val queries = sqlu"""BEGIN TRANSACTION""" +: records.map(recordToInsertQuery(_)) :+ sqlu"""END TRANSACTION"""
        DBIO.seq(queries:_*)
    }
}