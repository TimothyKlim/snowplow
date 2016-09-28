/**
  * Copyright (c) 2014-2016 Snowplow Analytics Ltd. All rights reserved.
  *
  * This program is licensed to you under the Apache License Version 2.0,
  * and you may not use this file except in compliance with the Apache License Version 2.0.
  * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the Apache License Version 2.0 is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
  */
import sbt._

object Dependencies {

  val resolutionRepos = Seq(
    // For Snowplow and amazon-kinesis-connectors
    "Snowplow Analytics Maven releases repo" at "http://maven.snplow.com/releases/",
    "Snowplow Analytics Maven snapshot repo" at "http://maven.snplow.com/snapshots/",
    // For Scalazon
    "BintrayJCenter" at "http://jcenter.bintray.com",
    // For user-agent-utils
    "user-agent-utils repo" at "https://raw.github.com/HaraldWalker/user-agent-utils/mvn-repo/",
    Resolver.bintrayRepo("fcomb", "maven")
  )

  object V {
    // Java
    val logback = "1.1.7"
    // Scala
    val akka = "2.4.10"
    val akkaStreamKafka = "0.12"
    val argot = "1.0.4"
    val config = "1.3.0"
    val elastic4s = "2.3.1"
    val igluClient = "0.3.2"
    val scalaz = "7.2.6"
    val snowplowCommonEnrich = "0.25.0-kt"
    val snowplowTracker = "0.4.0-kt"
    val scalaLogging = "3.5.0"
    // Scala (test only)
    val specs2 = "3.7"
    val scalazSpecs2 = "0.4.0"
    // Scala (compile only)
    val commonsLang3 = "3.4"
  }

  object Libraries {
    // Java
    val logback = "ch.qos.logback" %  "logback-classic" % V.logback
    // Scala
    val akkaSlf4j = "com.typesafe.akka" %% "akka-slf4j" % V.akka
    val akkaStreamKafka = "com.typesafe.akka" %% "akka-stream-kafka" % V.akkaStreamKafka
    val elastic4s = "com.sksamuel.elastic4s" %% "elastic4s-core" % V.elastic4s
    val argot = "org.clapper" %% "argot" % V.argot
    val config = "com.typesafe" % "config" % V.config
    val scalaz = "org.scalaz" %% "scalaz-core" % V.scalaz
    val snowplowTracker = "com.snowplowanalytics" %% "snowplow-scala-tracker" % V.snowplowTracker
      val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % V.scalaLogging
    // Intransitive to prevent the jar containing more than 2^16 files
    val snowplowCommonEnrich =
      "com.snowplowanalytics" %% "snowplow-common-enrich" % V.snowplowCommonEnrich intransitive
    // Since Common Enrich is intransitive, we explicitly add Iglu Scala Client as a dependency
    val igluClient = "com.snowplowanalytics" % "iglu-scala-client" % V.igluClient
    // Scala (test only)
    val specs2 = "org.specs2" %% "specs2" % V.specs2 % "test"
    val scalazSpecs2 = "org.typelevel" %% "scalaz-specs2" % V.scalazSpecs2 % "test"
    // Scala (compile only)
    val commonsLang3 = "org.apache.commons" % "commons-lang3" % V.commonsLang3 % "compile"
  }
}
