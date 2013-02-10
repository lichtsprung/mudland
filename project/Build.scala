import sbt._
import Keys._
import sbt._

object Mudland extends Build {

  lazy val projectSettings = Defaults.defaultSettings ++ Seq(
    name := "MUDland",
    version := "0.0.0",
    organization := "org.mudland",
    scalaVersion := "2.10.0",
    fork in run := true,
    libraryDependencies += Dependency.akkaActor,
    libraryDependencies += Dependency.akkaRemote,
    libraryDependencies += Dependency.akkaTestkit,
    libraryDependencies += Dependency.sprayCan,
    libraryDependencies += Dependency.sprayHttpx,
    libraryDependencies += Dependency.sprayClient,
    libraryDependencies += Dependency.scalatest,
    resolvers += Resolvers.sonatypeSnapshotRepo,
    resolvers += Resolvers.typesafeReleaseRepo,
    resolvers += Resolvers.typesafeSnapshotRepo,
    resolvers += Resolvers.sprayReleaseRepo,
    resolvers += Resolvers.sprayNightlyRepo,
  )

  lazy val root = Project(id = "root", base = file("."), settings = projectSettings)
}


object Resolvers {
  lazy val typesafeReleaseRepo = "Typesafe Snapshot Repository" at "http://repo.typesafe.com/typesafe/snapshots/"
  lazy val typesafeSnapshotRepo = "Typesafe Release Repository" at "http://repo.typesafe.com/typesafe/releases/"
  lazy val sonatypeSnapshotRepo = "Sonatype Snapshots Repository" at "https://oss.sonatype.org/content/repositories/snapshots/"
  lazy val sprayNightlyRepo = "Spray.io Nightly Repository" at " http://nightlies.spray.io"
  lazy val sprayReleaseRepo = "Spray.io Release Repository" at "http://repo.spray.io"
  lazy val bibsonomyRepo = "Bibsonomy Repository" at "http://dev.bibsonomy.org/maven2/"
}


object Dependency {
  lazy val akkaActor = "com.typesafe.akka" %% "akka-actor" % "2.1.0" withSources() withJavadoc()
  lazy val akkaRemote = "com.typesafe.akka" %% "akka-remote" % "2.1.0" withSources() withJavadoc()
  lazy val akkaTestkit = "com.typesafe.akka" %% "akka-testkit" % "2.1.0" withSources() withJavadoc()
  lazy val akkaCamel = "com.typesafe.akka" %% "akka-camel" % "2.1.0" withSources() withJavadoc()
  lazy val sprayCan = "io.spray" % "spray-can" % "1.1-20130207" withSources() withJavadoc()
  lazy val sprayHttpx = "io.spray" % "spray-httpx" % "1.1-20130207" withSources() withJavadoc()
  lazy val sprayClient = "io.spray" % "spray-client" % "1.1-20130207" withSources() withJavadoc()
  lazy val sprayJson = "io.spray" % "spray-json_2.10.0-RC3" % "1.2.3" withSources() withJavadoc()
  lazy val jenaCore = "org.apache.jena" % "jena-core" % "2.7.4" withSources() withJavadoc()
  lazy val jenaARQ = "org.apache.jena" % "jena-arq" % "2.9.4" withSources() withJavadoc()
  lazy val tikaParsers = "org.apache.tika" % "tika-parsers" % "1.2" withSources() withJavadoc()
  lazy val luceneCore = "org.apache.lucene" % "lucene-core" % "4.0.0" withSources() withJavadoc()
  lazy val luceneAnalyzers = "org.apache.lucene" % "lucene-analyzers-common" % "4.0.0" withSources() withJavadoc()
  lazy val bibsonomyREST = "org.bibsonomy" % "bibsonomy-rest-client" % "2.0.1" withSources()
  lazy val scalatest = "org.scalatest" % "scalatest_2.10" % "1.9.1" % "test" withSources() withJavadoc()
}
