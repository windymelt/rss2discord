package io.github.windymelt.rss2discord

import com.rometools.rome.feed.synd.SyndEntry
import java.time.{OffsetDateTime => DateTime}
import java.time.ZoneId
import java.time.ZoneOffset

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
    _.toInstant()
      .atZone(ZoneId.of(ZoneOffset.ofHours(tzOffset).getId()))
      .toOffsetDateTime()
  }
  def updatedDateTime: Option[DateTime] = Option(e.getUpdatedDate).map {
    _.toInstant()
      .atZone(ZoneId.of(ZoneOffset.ofHours(tzOffset).getId()))
      .toOffsetDateTime()
  }
