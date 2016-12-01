lazy val akkaStreamKafkaVersion      = "0.13"
lazy val akkaVersion                 = "2.4.14"
lazy val elastic4sVersion            = "5.0.1"
lazy val igluClientVersion           = "0.3.2"
lazy val postgresqlVersion           = "9.4.1212"
lazy val scalazVersion               = "7.2.7"
lazy val snowplowCommonEnrichVersion = "0.25.0-kt"
lazy val snowplowTrackerVersion      = "0.4.1-kt"

lazy val buildSettings = Seq(
  organization := "com.snowplowanalytics",
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
  "-Xms1g",
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
  .enablePlugins(JavaAppPackaging)
  .settings(allSettings: _*)
  .settings(noPublishSettings)
  .settings(
    libraryDependencies ++= Seq(
      "ch.qos.logback"                 % "logback-classic"         % "1.1.7",
      "com.fasterxml.jackson.datatype" % "jackson-datatype-jsr310" % "2.8.5",
      "com.sksamuel.elastic4s"         %% "elastic4s-core"         % elastic4sVersion,
      "com.snowplowanalytics"          % "iglu-scala-client"       % igluClientVersion,
      "com.snowplowanalytics" %% "snowplow-common-enrich" % snowplowCommonEnrichVersion intransitive,
      "com.snowplowanalytics"      %% "snowplow-scala-tracker" % snowplowTrackerVersion,
      "com.typesafe"               % "config"                  % "1.3.1",
      "com.typesafe.akka"          %% "akka-slf4j"             % akkaVersion,
      "com.typesafe.akka"          %% "akka-stream-kafka"      % akkaStreamKafkaVersion,
      "com.typesafe.scala-logging" %% "scala-logging"          % "3.5.0",
      "com.zaxxer"                 % "HikariCP"                % "2.5.1",
      "io.fcomb"                   %% "db-migration"           % "0.3.4",
      "org.apache.commons"         % "commons-lang3"           % "3.5" % "compile",
      "org.clapper"                %% "argot"                  % "1.0.4",
      "org.postgresql"             % "postgresql"              % postgresqlVersion exclude ("org.slf4j", "slf4j-simple"),
      "org.scalaz"                 %% "scalaz-core"            % scalazVersion,
      "org.tpolecat"               %% "doobie-core"            % "0.3.1-M2",
      "org.specs2"                 %% "specs2"                 % "3.7" % "test",
      "org.typelevel"              %% "scalaz-specs2"          % "0.5.0" % "test"
    ),
    mainClass := Some("com.snowplowanalytics.snowplow.storage"),
    executableScriptName := "start",
    javaOptions in Universal ++= javaRunOptions.map(o => s"-J$o"),
    packageName in Universal := "dist",
    javaOptions in (Test, run) ++= javaRunOptions,
    sourceGenerators in Compile += Def.task {
      val file = (sourceManaged in Compile).value / "settings.scala"
      IO.write(file,
               """package com.snowplowanalytics.snowplow.storage.generated
                 |object Settings {
                 |  val organization = "%s"
                 |  val version = "%s"
                 |  val name = "%s"
                 |}
                 |""".stripMargin.format(organization.value, version.value, name.value))
      Seq(file)
    }.taskValue,
    parallelExecution := true,
    fork in run := true
  )
