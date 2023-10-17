package io.github.windymelt.rss2discord

import com.rometools.rome.feed.synd.SyndEntry
import com.github.nscala_time.time.Imports._

case class RssEntry(
    title: String,
    link: String,
    author: String,
    publishedAt: Option[DateTime],
    updatedAt: Option[DateTime],
)

extension (e: SyndEntry)
  def asRssEntry: RssEntry = RssEntry(
    e.getTitle(),
    e.getLink(),
    e.getAuthor(),
    e.publishedDateTime,
    e.updatedDateTime,
  )
  def publishedDateTime: Option[DateTime] = Option(e.getPublishedDate).map {
    _.toLocalDateTime
      .toDateTime(DateTimeZone.forOffsetHours(tzOffset))
  }
  def updatedDateTime: Option[DateTime] = Option(e.getUpdatedDate).map {
    _.toLocalDateTime
      .toDateTime(DateTimeZone.forOffsetHours(tzOffset))
  }
