package io.github.windymelt.rss2discord

import com.github.nscala_time.time.Imports._

class Rss2DiscordSpec extends munit.FunSuite {
  test(
    "filterEntriesByPublished can filter entries by published/updated time",
  ) {
    val e1 = RssEntry(
      "e1",
      "link",
      "author",
      Some(DateTime.parse("2023-01-01T00:00:00+09:00")),
      None,
    )
    val e2 = RssEntry(
      "e2",
      "link",
      "author",
      Some(DateTime.parse("2023-01-01T00:15:00+09:00")),
      None,
    )
    val e3 = RssEntry(
      "e3",
      "link",
      "author",
      Some(DateTime.parse("2023-01-01T00:30:00+09:00")),
      None,
    )
    val e4 = RssEntry(
      "e4",
      "link",
      "author",
      Some(DateTime.parse("2023-01-01T00:45:00+09:00")),
      None,
    )

    val merkmal = DateTime.parse("2023-01-01T00:15:00+09:00")
    val filtered =
      Rss2Discord.filterEntriesByPublished(Seq(e1, e2, e3, e4), merkmal)
    val expected = Seq(e3, e4)
    assertEquals(filtered, expected)
  }
}
