package io.github.windymelt.rss2discord

import com.rometools.rome.feed.synd.SyndEntry
import com.github.nscala_time.time.Imports._

extension (e: SyndEntry)
  def publishedDateTime: Option[DateTime] = Option(e.getPublishedDate).map {
    _.toLocalDateTime
      .toDateTime(DateTimeZone.forOffsetHours(tzOffset))
  }
  def updatedDateTime: Option[DateTime] = Option(e.getUpdatedDate).map {
    _.toLocalDateTime
      .toDateTime(DateTimeZone.forOffsetHours(tzOffset))
  }
