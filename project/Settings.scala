import sbt.Keys._
import sbt._

trait Settings { this: Build =>

  val additionalScalacOptions = Seq("-Xlint:-missing-interpolator","-Xfatal-warnings","-deprecation","-unchecked","-feature","-language:implicitConversions","-language:postfixOps")

  val javaTestOptions = Seq("-XX:MaxPermSize=128m", "-Xms512m", "-Xmx512m")

  val appVersion = "1.3.0.7"
  val buildNumber = sys.env.getOrElse("CIRCLE_BUILD_NUM", default = "0")
  val branchName = sys.env.getOrElse("CIRCLE_BRANCH", default = "LOCAL").filter(_.isLetterOrDigit).toUpperCase
  val appMode = sys.props.getOrElse("application.mode", "DEV").toUpperCase

  private val alReleaseCredential = {
    if (sys.env.contains("CI")) {
      Credentials(
        "Nexus Repository Manager",
        "nexus.angieslist.com",
        "deployment",
        sys.env.getOrElse("ARTIFACTORY_PASSWORD", default = throw new Exception("Password not found"))
      )
    } else {
      Credentials("Nexus Repository Manager", "nexus.angieslist.com", "deployment", "EZaU22woREzvPvri")
    }
  }

  lazy val projectSettings: Seq[Def.Setting[_]] = Seq(
    scalaVersion := "2.11.7",
    organization := "com.angieslist",
    scalacOptions ++= additionalScalacOptions,
    javaOptions in Test ++= javaTestOptions,
    exportJars := true,
    credentials := Seq(
      alReleaseCredential,
      Credentials("Nexus Repository Manager", "nexus.angieslist.com", "deployment", "EZaU22woREzvPvri"),
      Credentials("snapshots", "nexus.angieslist.com", "deployment", "EZaU22woREzvPvri"),
      Credentials("3rd party", "nexus.angieslist.com", "deployment", "EZaU22woREzvPvri")
    ),
    publishMavenStyle := true,
    version := {
      if(appMode == "PROD") s"$appVersion.$buildNumber"
      else s"$appVersion-$branchName-SNAPSHOT"
    },
    publishTo := {
      if(isSnapshot.value) Some("snapshots" at "https://nexus.angieslist.com/nexus/content/repositories/snapshots")
      else Some("Nexus Repository Manager" at "https://nexus.angieslist.com/nexus/content/repositories/releases")
    },
    resolvers ++= Seq(
      "Maven Central - Releases" at "http://repo1.maven.org/maven2",
      "Maven Central Central" at "http://central.maven.org/maven2",
      "Typesafe Maven - Releases" at "http://dl.bintray.com/typesafe/maven-releases/",
      "JBoss Public" at "https://repository.jboss.org/nexus/content/groups/public",
      "Sonatype Public" at "https://oss.sonatype.org/content/repositories/releases/",
      "Sonatype Maven Releases" at "https://oss.sonatype.org/content/groups/public",
      "Sonatype - Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/",
      "Etaty - Releases" at "http://dl.bintray.com/etaty/maven",
      "Eaio" at "http://eaio.com/maven2",
      "Nexus Repository Manager" at "https://nexus.angieslist.com/nexus/content/repositories/releases",
      "snapshots" at "https://nexus.angieslist.com/nexus/content/repositories/snapshots",
      "3rd party" at "https://nexus.angieslist.com/nexus/content/repositories/thirdparty"
    ),
    publishArtifact in packageSrc := true,
    publishArtifact in Test := true,
    // enable improved incremental compilation algorithm called "name hashing"
    incOptions := incOptions.value.withNameHashing(true),
    doc in Compile <<= target.map(_ / "none")
  )

}