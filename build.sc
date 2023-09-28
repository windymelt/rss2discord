import $ivy.`de.tototec::de.tobiasroeser.mill.vcs.version::0.4.0`
import de.tobiasroeser.mill.vcs.version.VcsVersion

import mill._, scalalib._
import publish._

object rss2discord extends RootModule with ScalaModule with PublishModule {
  def scalaVersion = "3.3.0"
  def ivyDeps = Agg(
    ivy"com.amazonaws:aws-lambda-java-runtime-interface-client::2.3.2",
    ivy"com.amazonaws:aws-lambda-java-core::1.2.2",
    ivy"com.rometools:rome:2.1.0",
    ivy"com.softwaremill.sttp.tapir::tapir-core:1.7.3",
    ivy"com.softwaremill.sttp.tapir::tapir-http4s-client:1.7.3",
    ivy"com.softwaremill.sttp.tapir::tapir-json-circe:1.7.3",
    ivy"org.http4s::http4s-blaze-client:0.23.15",
    ivy"com.github.nscala-time::nscala-time:2.32.0"
  )

  def scalaDocOptions = Seq("-siteroot", "docs")

  // Ensure AWS Lambda to extract JAR file
  override def prependShellScript: T[String] = ""
  override def artifactName = "rss2discord"
  override def sonatypeUri = "https://s01.oss.sonatype.org/service/local"
  override def sonatypeSnapshotUri =
    "https://s01.oss.sonatype.org/content/repositories/snapshots"
  def pomSettings = PomSettings(
    description = artifactName(),
    organization = "io.github.windymelt",
    url = "https://github.com/windymelt/rss2discord",
    licenses = Seq(License.`BSD-2-Clause`),
    versionControl = VersionControl.github("windymelt", "rss2discord"),
    developers = Seq(
      Developer("windymelt", "windymelt", "https://github.com/windymelt")
    )
  )
  override def publishVersion: T[String] = VcsVersion.vcsState().format()
}
