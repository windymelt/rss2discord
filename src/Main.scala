package io.github.windymelt.rss2discord

import cats.effect.IO
import cats.effect.IOApp
import com.amazonaws.services.lambda.runtime.Context
import com.github.nscala_time.time.Imports._
import com.rometools.rome.feed._
import com.rometools.rome.feed.synd.SyndEntry
import com.rometools.rome.io.SyndFeedInput
import com.rometools.rome.io.XmlReader
import io.github.windymelt.rss2discord.endpoint.Discord
import org.http4s.blaze.client.BlazeClientBuilder
import sttp.tapir.client.http4s.Http4sClientInterpreter

import java.io.InputStream
import java.io.OutputStream
import java.net.URL
import scala.concurrent.duration.FiniteDuration

val feedUrlEnv: Option[String] = sys.env.get("RSS_URL")
val webhookUrl: String = sys.env("WEBHOOK_URL")
val tzOffset: Int = sys.env
  .get("TZ_OFFSET")
  .map(_.toInt)
  .getOrElse(
    FiniteDuration(
      DateTime.now().zone.getOffset(DateTime.now()),
      "milliseconds"
    ).toHours.toInt
  )

extension (e: SyndEntry)
  def publishedDateTime: Option[DateTime] = Option(e.getPublishedDate).map {
    _.toLocalDateTime
      .toDateTime(DateTimeZone.forOffsetHours(tzOffset))
  }
  def updatedDateTime: Option[DateTime] = Option(e.getUpdatedDate).map {
    _.toLocalDateTime
      .toDateTime(DateTimeZone.forOffsetHours(tzOffset))
  }

object Rss2Discord extends IOApp.Simple {
  def handler(in: InputStream, out: OutputStream, ctx: Context): Unit = main(
    Array()
  )
  def run: IO[Unit] = {
    import cats.implicits._
    import scala.collection.JavaConverters._

    val feedUrl = feedUrlEnv.get

    def entriesToPost(timeAfter: DateTime) = IO.delay {
      val feed = new SyndFeedInput().build(new XmlReader(new URL(feedUrl)))
      feed
        .getEntries()
        .asScala
        .filter {
          case e if (e.publishedDateTime orElse e.updatedDateTime).isDefined =>
            val dt = (e.publishedDateTime orElse e.updatedDateTime).get
            dt.isAfter(timeAfter)
          case _ => false
        }
    }
    for {
      dt <- IO(DateTime.now())
      entries <- entriesToPost(timeAfter = dt.minusMinutes(31))
    } yield entries.toSeq.traverse(e => post(formatEntry(e))).void
  }

  def formatEntry(entry: SyndEntry): String = {
    s"""[${entry.getTitle()}](${entry.getLink()})
Author: ${entry.getAuthor()}
"""
  }

  def post(content: String): IO[Unit] = {
    val (req, res) =
      Http4sClientInterpreter[IO]()
        .toRequest(
          endpoint.Discord.webhook,
          Some(org.http4s.Uri.unsafeFromString(webhookUrl))
        )
        .apply(endpoint.WebhookInput(content))

    val clientResource = BlazeClientBuilder[IO].resource
    val parsedResult =
      clientResource.flatMap(_.run(req)).use(res)

    parsedResult.debug().void
  }
}
