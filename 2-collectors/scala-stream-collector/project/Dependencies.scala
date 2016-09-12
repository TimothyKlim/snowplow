/*
 * Copyright (c) 2013-2016 Snowplow Analytics Ltd. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0, and
 * you may not use this file except in compliance with the Apache License
 * Version 2.0.  You may obtain a copy of the Apache License Version 2.0 at
 * http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Apache License Version 2.0 is distributed on an "AS
 * IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the Apache License Version 2.0 for the specific language
 * governing permissions and limitations there under.
 */
import sbt._

object Dependencies {

  val resolutionRepos = Seq(
    "Snowplow Analytics Maven repo" at "http://maven.snplow.com/releases/",
    "Snowplow Analytics Maven snapshot repo" at "http://maven.snplow.com/snapshots/",
    "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/",
    // For Scalazon
    "BintrayJCenter" at "http://jcenter.bintray.com",
    // For sbt-thrift
    "bigtoast-github" at "http://bigtoast.github.com/repo/"
  )

  object V {
    // Java
    val mimepull = "1.9.6"
    val awsSdk = "1.11.33"
    val yodaTime = "2.9.4"
    val yodaConvert = "1.8.1"
    // Scala
    val snowplowCommonEnrich = "0.22.0"
    val igluClient = "0.3.2"
    val scalaz7 = "7.2.6"
    val snowplowRawEvent = "0.1.0"
    val collectorPayload = "0.0.0"
    val akka = "2.4.10"
    val logback = "1.1.7"
    val commonsCodec = "1.10"
    val scalazon = "0.11"
    val argot = "1.0.4"
    val json4s = "3.4.0"
    // Scala (test only)
    val scalaTest = "3.0.0"
  }

  object Libraries {
    // Java
    val mimepull = "org.jvnet.mimepull" % "mimepull" % V.mimepull
    val awsSdk = "com.amazonaws" % "aws-java-sdk" % V.awsSdk
    val yodaTime = "joda-time" % "joda-time" % V.yodaTime
    val yodaConvert = "org.joda" % "joda-convert" % V.yodaConvert

    // Scala
    // Exclude netaporter to prevent conflicting cross-version suffixes for shapeless
    val snowplowCommonEnrich =
      "com.snowplowanalytics" % "snowplow-common-enrich" % V.snowplowCommonEnrich intransitive
    val igluClient = "com.snowplowanalytics" % "iglu-scala-client" % V.igluClient
    val scalaz7 = "org.scalaz" %% "scalaz-core" % V.scalaz7
    val snowplowRawEvent = "com.snowplowanalytics" % "snowplow-thrift-raw-event" % V.snowplowRawEvent
    val collectorPayload = "com.snowplowanalytics" % "collector-payload-1" % V.collectorPayload
    val argot = "org.clapper" %% "argot" % V.argot
    val akkaHttp = "com.typesafe.akka" %% "akka-http-experimental" % V.akka
    val akkaSlf4j = "com.typesafe.akka" %% "akka-slf4j" % V.akka
    val logback = "ch.qos.logback" % "logback-classic" % V.logback
    val commonsCodec = "commons-codec" % "commons-codec" % V.commonsCodec
    val scalazon = "io.github.cloudify" %% "scalazon" % V.scalazon
    val json4sJackson = "org.json4s" %% "json4s-jackson" % V.json4s

    // Scala (test only)
    val scalaTest = "org.scalatest"      %% "scalatest"          % V.scalaTest % "test"
    val akkaHttpTestkit = "com.typesafe.akka" %% "akka-http-testkit" % V.akka % "test"
  }
}
