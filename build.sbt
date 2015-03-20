name := """prototype-1"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  cache,
  ws,
  "org.webjars" % "rot.js" % "0.5.0",
  "org.julienrf" %% "play-json-variants" % "1.1.0"
)
