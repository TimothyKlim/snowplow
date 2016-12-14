/**
  * Copyright (c) 2014-2016 Snowplow Analytics Ltd.
  * All rights reserved.
  *
  * This program is licensed to you under the Apache License Version 2.0,
  * and you may not use this file except in compliance with the Apache
  * License Version 2.0.
  * You may obtain a copy of the Apache License Version 2.0 at
  * http://www.apache.org/licenses/LICENSE-2.0.
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the Apache License Version 2.0 is distributed
  * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
  * either express or implied.
  *
  * See the Apache License Version 2.0 for the specific language
  * governing permissions and limitations there under.
  */
package com.snowplowanalytics.snowplow.storage

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl._
import com.snowplowanalytics.snowplow.enrich.common.outputs.BadRow
import com.snowplowanalytics.snowplow.scalatracker.emitters.AsyncEmitter
import com.snowplowanalytics.snowplow.scalatracker.SelfDescribingJson
import com.snowplowanalytics.snowplow.scalatracker.Tracker
import com.snowplowanalytics.snowplow.storage.source._
import com.snowplowanalytics.snowplow.storage.utils.Tracking
import com.typesafe.config.{Config, ConfigFactory}
import java.io.File
import java.util.Properties
import org.clapper.argot.ArgotParser
import org.json4s.jackson.JsonMethods._
import org.json4s.JsonDSL._
import org.json4s._
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import _root_.scalaz._, Scalaz._

/**
  * Main entry point for the Elasticsearch sink
  */
object Main extends App {
  val parser = new ArgotParser(
    programName = generated.Settings.name,
    compactUsage = true,
    preUsage = Some(
      "%s: Version %s. Copyright (c) 2013, %s.".format(generated.Settings.name,
                                                       generated.Settings.version,
                                                       generated.Settings.organization))
  )

  val defaultConfig = ConfigFactory.load()

  // Optional config argument
  val config = parser.option[Config](List("config"),
                                     "filename",
                                     """
                                       |Configuration file""".stripMargin) { (c, opt) =>
    val file = new File(c)
    if (file.exists) ConfigFactory.parseFile(file)
    else {
      parser.usage("Configuration file \"%s\" does not exist".format(c))
      ConfigFactory.empty()
    }
  }

  parser.parse(args)

  val configValue: Config = config.value
    .getOrElse(throw new RuntimeException("--config argument must be provided"))
    .resolve
    .getConfig("storage")

  lazy val streamType = ??? /*configValue.getString("stream-type") match {
    case "good" => StreamType.Good
    case "bad" => StreamType.Bad
    case _ =>
      throw new RuntimeException(
        """"stream-type" must be set to "good" or "bad"""")
  }*/

  val tracker =
    if (configValue.hasPath("monitoring.snowplow"))
      Tracking.initializeTracker(configValue.getConfig("monitoring.snowplow")).some
    else None

  val rawConf = config.value
    .map(_.withFallback(defaultConfig))
    .getOrElse(throw new RuntimeException("--config option must be provided"))

  val finalConfig = AppConfig(configValue)

  org.json4s.jackson.JsonMethods.mapper
    .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule())

  implicit val sys = ActorSystem.create("kafka-elasticsearch-sink", rawConf)
  implicit val mat = ActorMaterializer()
  import sys.dispatcher

  val inStream = finalConfig.source match {
    case KafkaSourceConfig(config) => new source.Kafka(config)
  }
  val outStream = finalConfig.sink match {
    case ElasticSinkConfig(config)  => new sink.Elastic(config)
    case PostgresSinkConfig(config) => new sink.Postgres(config)
  }

  inStream.source
    .map {
      case (msg, offset) => (Transformer.transform(msg), offset)
    }
    .collect { case ((_, Success(rec)), offset) => (rec, offset) }
    .via(outStream.flow)
    .runWith(inStream.commitSink)

  Await.result(sys.whenTerminated, Duration.Inf)
}
