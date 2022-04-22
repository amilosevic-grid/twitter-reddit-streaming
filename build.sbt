name := "twitter-reddit-streaming"
version := "0.1"
scalaVersion := "2.12.11"
idePackagePrefix := Some("cole.streaming")

val versions = new Versions()
val dependencies = new Dependencies(versions)

lazy val commons = project
  .in(file("commons"))
  .withId("twitter-reddit-streaming-commons")
  .settings(
    libraryDependencies ++= dependencies.commonDeps
  ).settings(name := "twitter-reddit-streaming-commons")

lazy val producers = project
  .in(file("producers"))
  .withId("twitter-reddit-streaming-producers")
  .settings(
    libraryDependencies ++= dependencies.kafka,
    libraryDependencies ++= dependencies.twitterAPIs,
    libraryDependencies ++= dependencies.commonDeps,
    resolvers += "Confluent Repo" at "https://packages.confluent.io/maven",
    resolvers += "Twitter" at "https://maven.twttr.com/"
  ).settings(name := "twitter-reddit-streaming-producers")
  .dependsOn(commons)

lazy val consumers = project
  .in(file("consumers"))
  .withId("twitter-reddit-streaming-consumers")
  .settings(
    libraryDependencies ++= dependencies.spark
  ).settings(name := "twitter-reddit-streaming-consumers")
  .dependsOn(commons)