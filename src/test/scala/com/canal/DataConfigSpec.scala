package com.canal

import org.scalatest._
import flatspec._
import matchers._
import com.canal.config.DataConfig

class DataConfigSpec extends AnyFlatSpec with should.Matchers {

    "DataConfig object" should "provide titles file path" in {
        val titleFilePath = DataConfig.TITLES_FILE

        titleFilePath.toString should endWith ("data/title.basics.tsv")
    }

    "It" should "provide title id column name" in {
        val titleIdColumn = DataConfig.TITLES_ID

        titleIdColumn should be ("tconst")
    }
}