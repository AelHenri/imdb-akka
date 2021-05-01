package com.canal.models

case class Episode (
    episodeId: String,
    parentId: String,
    seasonNumber: Option[Int],
    episodeNumber: Option[Int]
)