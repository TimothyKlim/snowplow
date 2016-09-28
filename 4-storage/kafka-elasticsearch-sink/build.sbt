lazy val akkaStreamKafkaVersion      = "0.12"
lazy val akkaVersion                 = "2.4.10"
lazy val elastic4sVersion            = "2.3.1"
lazy val igluClientVersion           = "0.3.2"
lazy val scalazVersion               = "7.2.6"
lazy val snowplowCommonEnrichVersion = "0.25.0-kt"
lazy val snowplowTrackerVersion      = "0.4.0-kt"

lazy val buildSettings = Seq(
  organization := "com.snowplowanalytics",
  version := "0.8.0-kt",
  description := "Kafka sink for Elasticsearch",
  scalaVersion := "2.11.8",
  scalafmtConfig := Some(file(".scalafmt.conf"))
)

lazy val commonSettings =
  reformatOnCompileSettings ++
    Seq(
      resolvers ++= Seq(
        // For Snowplow
        "Snowplow Analytics Maven releases repo" at "http://maven.snplow.com/releases/",
        "Snowplow Analytics Maven snapshot repo" at "http://maven.snplow.com/snapshots/",
        // For Scalazon
        "BintrayJCenter" at "http://jcenter.bintray.com",
        // For user-agent-utils
        "user-agent-utils repo" at "https://raw.github.com/HaraldWalker/user-agent-utils/mvn-repo/",
        Resolver.bintrayRepo("fcomb", "maven")
      ),
      scalacOptions := Seq("-encoding",
                           "UTF-8",
                           "-target:jvm-1.8",
                           "-unchecked",
                           "-deprecation",
                           "-feature",
                           "-language:higherKinds",
                           "-language:existentials",
                           "-language:postfixOps",
                           "-Xexperimental",
                           "-Xlint",
                           // "-Xfatal-warnings",
                           "-Xfuture",
                           "-Ybackend:GenBCode",
                           "-Ydelambdafy:method",
                           "-Yno-adapted-args",
                           "-Yopt-warnings",
                           "-Yopt:l:classpath",
                           "-Yopt:unreachable-code" /*,
                           "-Ywarn-dead-code",
                           "-Ywarn-infer-any",
                           "-Ywarn-numeric-widen",
                           "-Ywarn-unused",
                           "-Ywarn-unused-import",
                           "-Ywarn-value-discard"*/ ),
      sources in (Compile, doc) := Seq.empty,
      shellPrompt := { s =>
        Project.extract(s).currentProject.id + " > "
      }
    )

lazy val noPublishSettings = Seq(
  publish := (),
  publishLocal := (),
  publishArtifact := false
)

lazy val allSettings = buildSettings ++ commonSettings

lazy val javaRunOptions = Seq(
  "-server",
  "-XX:+UseAES",
  "-XX:+UseAESIntrinsics",
  "-Xms2g",
  "-Xmx2g",
  "-Xss6m",
  "-XX:NewSize=512m",
  "-XX:+UseNUMA",
  "-XX:+AlwaysPreTouch",
  "-XX:-UseBiasedLocking",
  "-XX:+TieredCompilation",
  "-XX:+UseG1GC",
  "-XX:MaxGCPauseMillis=200",
  "-XX:ParallelGCThreads=20",
  "-XX:ConcGCThreads=5",
  "-XX:InitiatingHeapOccupancyPercent=70"
)

lazy val root = Project(id = "snowplow-elasticsearch-sink", base = file("."))
  .settings(allSettings: _*)
  .settings(noPublishSettings)
  .settings(SbtNativePackager.packageArchetype.java_application)
  .settings(
    libraryDependencies ++= Seq(
      "ch.qos.logback"             % "logback-classic"         % "1.1.7",
      "com.sksamuel.elastic4s"     %% "elastic4s-core"         % elastic4sVersion,
      "com.snowplowanalytics"      % "iglu-scala-client"       % igluClientVersion,
      "com.snowplowanalytics"      %% "snowplow-common-enrich" % snowplowCommonEnrichVersion intransitive,
      "com.snowplowanalytics"      %% "snowplow-scala-tracker" % snowplowTrackerVersion,
      "com.typesafe"               % "config"                  % "1.3.1",
      "com.typesafe.akka"          %% "akka-slf4j"             % akkaVersion,
      "com.typesafe.akka"          %% "akka-stream-kafka"      % akkaStreamKafkaVersion,
      "com.typesafe.scala-logging" %% "scala-logging"          % "3.5.0",
      "org.apache.commons"         % "commons-lang3"           % "3.4" % "compile",
      "org.clapper"                %% "argot"                  % "1.0.4",
      "org.scalaz"                 %% "scalaz-core"            % scalazVersion,
      "org.specs2"                 %% "specs2"                 % "3.7" % "test",
      "org.typelevel"              %% "scalaz-specs2"          % "0.4.0" % "test"
    ),
    mainClass := Some("com.snowplowanalytics.snowplow.storage"),
    executableScriptName := "start",
    javaOptions in Universal ++= javaRunOptions.map(o => s"-J$o"),
    packageName in Universal := "dist",
    javaOptions in (Test, run) ++= javaRunOptions,
    sourceGenerators in Compile <+= (sourceManaged in Compile, version, name, organization) map {
      (d, v, n, o) =>
        val file = d / "settings.scala"
        IO.write(file,
                 """package com.snowplowanalytics.snowplow.storage.generated
                   |object Settings {
                   |  val organization = "%s"
                   |  val version = "%s"
                   |  val name = "%s"
                   |}
                   |""".stripMargin.format(o, v, n))
        Seq(file)
    },
    parallelExecution := true,
    fork in run := true
  )
  .enablePlugins(SbtNativePackager)
