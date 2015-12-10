name := "twit-remote"

organization := "com.github.synesso"

version := "1.0.0"

scalaVersion := "2.11.6"

resolvers ++= Seq(
  "softprops" at "http://dl.bintray.com/content/softprops/maven"
)

libraryDependencies ++= Seq(
  "com.typesafe" % "config" % "1.2.1",
  "io.spray" %%  "spray-json" % "1.3.1",
  "me.lessis" %% "courier" % "0.1.3",
  "org.twitter4j" % "twitter4j-stream" % "4.0.4"
)

scalacOptions ++= Seq("-feature", "-language:implicitConversions")
