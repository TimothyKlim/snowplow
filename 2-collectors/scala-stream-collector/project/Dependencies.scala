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
    "bigtoast-github" at "http://bigtoast.github.com/repo/",
    Resolver.bintrayRepo("fcomb", "maven")
  )

  object V {
    // Java
    val mimepull = "1.9.6"
    val awsSdk = "1.11.33"
    // Scala
    val akka = "2.4.14"
    val akkaHttp = "10.0.0"
    val akkaStreamKafka = "0.13"
    val argot = "1.0.4"
    val collectorPayload = "0.0.0"
    val commonsCodec = "1.10"
    val igluClient = "0.5.0-kt"
    val json4s = "3.4.0"
    val logback = "1.1.7"
    val scalaz = "7.2.6"
    val scalazon = "0.11"
    val snowplowCommonEnrich = "0.25.0-kt"
    val snowplowRawEvent = "0.1.0"
    // Scala (test only)
    val scalaTest = "3.0.1"
  }

  object Libraries {
    // Java
    val mimepull = "org.jvnet.mimepull" % "mimepull" % V.mimepull
    val awsSdk = "com.amazonaws" % "aws-java-sdk" % V.awsSdk

    // Scala
    // Exclude netaporter to prevent conflicting cross-version suffixes for shapeless
    val akkaHttp = "com.typesafe.akka" %% "akka-http" % V.akkaHttp
    val akkaStreamKafka = "com.typesafe.akka" %% "akka-stream-kafka" % V.akkaStreamKafka
    val akkaSlf4j = "com.typesafe.akka" %% "akka-slf4j" % V.akka
    val argot = "org.clapper" %% "argot" % V.argot
    val collectorPayload = "com.snowplowanalytics" % "collector-payload-1" % V.collectorPayload
    val commonsCodec = "commons-codec" % "commons-codec" % V.commonsCodec
    val igluClient = "com.snowplowanalytics" %% "iglu-scala-client" % V.igluClient
    val json4sJackson = "org.json4s" %% "json4s-jackson" % V.json4s
    val logback = "ch.qos.logback" % "logback-classic" % V.logback
    val scalaz = "org.scalaz" %% "scalaz-core" % V.scalaz
    val scalazon = "io.github.cloudify" %% "scalazon" % V.scalazon
    val snowplowCommonEnrich =
      "com.snowplowanalytics" %% "snowplow-common-enrich" % V.snowplowCommonEnrich intransitive
    val snowplowRawEvent = "com.snowplowanalytics" % "snowplow-thrift-raw-event" % V.snowplowRawEvent

    // Scala (test only)
    val scalaTest = "org.scalatest" %% "scalatest" % V.scalaTest % "provided"
    val akkaHttpTestkit = "com.typesafe.akka" %% "akka-http-testkit" % V.akkaHttp % "provided"
  }
}
