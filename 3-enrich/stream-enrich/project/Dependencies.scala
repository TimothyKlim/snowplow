/*
 * Copyright (c) 2013-2016 Snowplow Analytics Ltd. All rights reserved.
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
    // For Snowplow
    "Snowplow Analytics Maven releases repo" at "http://maven.snplow.com/releases/",
    "Snowplow Analytics Maven snapshot repo" at "http://maven.snplow.com/snapshots/",
    // For Scalazon
    "BintrayJCenter" at "http://jcenter.bintray.com",
    // For uaParser utils
    "user-agent-parser repo" at "https://clojars.org/repo/",
    // For user-agent-utils
    "user-agent-utils repo" at "https://raw.github.com/HaraldWalker/user-agent-utils/mvn-repo/",
    Resolver.bintrayRepo("fcomb", "maven")
  )

  object V {
    // Java
    val awsSdk = "1.11.34"
    val httpClient = "4.5.2"
    val httpCore = "4.4.5"
    val jacksonCore = "2.8.2"
    val kinesisClient = "1.7.0"
    val logging = "1.2"
    val slf4j = "1.7.21"
    // Scala
    val akka = "2.4.10"
    val argot = "1.0.4"
    val config = "1.3.0"
    val igluClient = "0.5.0-kt"
    val scalaz = "7.2.6"
    val scalazon = "0.11"
    val snowplowCommonEnrich = "0.25.0-kt"
    val snowplowRawEvent = "0.1.0"
    val snowplowTracker = "0.4.0-kt"
    // Scala (test only)
    val akkaStreamKafka = "0.12"
    val specs2 = "3.7"
    val scalazSpecs2 = "0.4.0"
    // Scala (compile only)
    val commonsLang3 = "3.4"
    val thrift = "0.9.3"
  }

  object Libraries {
    // Java
    val logging = "commons-logging" % "commons-logging" % V.logging
    val httpCore = "org.apache.httpcomponents" % "httpcore" % V.httpCore
    val httpClient = "org.apache.httpcomponents" % "httpclient" % V.httpClient
    val jacksonCore = "com.fasterxml.jackson.core" % "jackson-core" % V.jacksonCore
    val slf4j = "org.slf4j" % "slf4j-simple" % V.slf4j
    val log4jOverSlf4j = "org.slf4j" % "log4j-over-slf4j" % V.slf4j
    val awsSdk = "com.amazonaws" % "aws-java-sdk" % V.awsSdk
    val kinesisClient = "com.amazonaws" % "amazon-kinesis-client" % V.kinesisClient
    // Scala
    val akkaSlf4j = "com.typesafe.akka" %% "akka-slf4j" % V.akka
    val akkaStreamKafka = "com.typesafe.akka" %% "akka-stream-kafka" % V.akkaStreamKafka
    val argot = "org.clapper" %% "argot" % V.argot
    val config = "com.typesafe" % "config" % V.config
    val igluClient = "com.snowplowanalytics" %% "iglu-scala-client" % V.igluClient
    val scalaz = "org.scalaz" %% "scalaz-core" % V.scalaz
    val scalazon = "io.github.cloudify" %% "scalazon" % V.scalazon
    val snowplowCommonEnrich = "com.snowplowanalytics" %% "snowplow-common-enrich" % V.snowplowCommonEnrich
    val snowplowRawEvent = "com.snowplowanalytics" % "snowplow-thrift-raw-event" % V.snowplowRawEvent
    val snowplowTracker = "com.snowplowanalytics" %% "snowplow-scala-tracker" % V.snowplowTracker
    // Scala (test only)
    val specs2 = "org.specs2" %% "specs2" % V.specs2 % "test"
    val scalazSpecs2 = "org.typelevel" %% "scalaz-specs2" % V.scalazSpecs2 % "test"
    // Scala (compile only)
    val commonsLang3 = "org.apache.commons" % "commons-lang3" % V.commonsLang3 % "compile"
    val thrift = "org.apache.thrift" % "libthrift" % V.thrift % "compile"
  }
}
