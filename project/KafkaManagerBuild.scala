import sbt._
import sbt.Keys._
import play.sbt.PlayScala

object KafkaManagerBuild extends Build with Settings {

  (aggregate in Global) in Test := false

  override def rootProject = Some(app)

  lazy val app = Project (
    id = "kafka-manager",
    base = file(".")
  ).settings(projectSettings:_*)
    .enablePlugins(PlayScala)

}