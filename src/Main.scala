package io.github.windymelt.rss2discord

import cats.effect.IO
import cats.effect.IOApp
import com.amazonaws.services.lambda.runtime.Context
import com.github.nscala_time.time.Imports._
import com.rometools.rome.feed._
import com.rometools.rome.feed.synd.SyndEntry
import com.rometools.rome.io.SyndFeedInput
import com.rometools.rome.io.XmlReader
import org.http4s.blaze.client.BlazeClientBuilder
import sttp.tapir.client.http4s.Http4sClientInterpreter

import java.io.InputStream
import java.io.OutputStream
import java.net.URL
import scala.concurrent.duration.FiniteDuration

val tzOffset: Int = sys.env
  .get("TZ_OFFSET")
  .map(_.toInt)
  .getOrElse(
    FiniteDuration(
      DateTime.now().zone.getOffset(DateTime.now()),
      "milliseconds",
    ).toHours.toInt,
  )

object Rss2Discord extends IOApp.Simple {
  def handler(in: InputStream, out: OutputStream, ctx: Context): Unit = main(
    Array(),
  )

  def filterEntriesByPublished(
      feeds: Seq[RssEntry],
      timeAfter: DateTime,
  ): Seq[RssEntry] = feeds.filter {
    case e if (e.publishedAt orElse e.updatedAt).isDefined =>
      val dt = (e.publishedAt orElse e.updatedAt).get
      dt.isAfter(timeAfter)
    case _ => false
  }

  private def entriesToPost(
      feedUrl: String,
      timeAfter: DateTime,
  ): IO[Seq[RssEntry]] = IO.delay {
    import scala.collection.JavaConverters._
    val feed = new SyndFeedInput().build(new XmlReader(new URL(feedUrl)))
    filterEntriesByPublished(
      feed
        .getEntries()
        .asScala
        .toSeq
        .map(_.asRssEntry),
      timeAfter,
    )
  }

  def run: IO[Unit] = {
    import cats.implicits._
    val feedUrlEnv: Option[String] = sys.env.get("RSS_URL")
    val webhookUrl: String = sys.env("WEBHOOK_URL")

    for {
      dt <- IO(DateTime.now())
      _ <- IO.println(s"finding entries ${dt.minusMinutes(31)} .. $dt")
      entries <- entriesToPost(feedUrlEnv.get, timeAfter = dt.minusMinutes(31))
      _ <- IO.println(s"entriesToPost: ${entries.map(_.title)}")
      _ <- entries.toSeq.traverse(e => Discord.post(webhookUrl, formatEntry(e)))
    } yield ()
  }

  def formatEntry(entry: RssEntry): String = {
    s"""[${entry.title}](${entry.link})
Author: ${entry.author}
"""
  }
}
