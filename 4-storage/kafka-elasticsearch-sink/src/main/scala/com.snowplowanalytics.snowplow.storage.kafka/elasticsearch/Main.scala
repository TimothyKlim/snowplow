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
package com.snowplowanalytics.snowplow.storage.kafka.elasticsearch

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.snowplowanalytics.snowplow.enrich.common.outputs.BadRow
import com.snowplowanalytics.snowplow.scalatracker.emitters.AsyncEmitter
import com.snowplowanalytics.snowplow.scalatracker.SelfDescribingJson
import com.snowplowanalytics.snowplow.scalatracker.Tracker
import com.typesafe.config.{Config, ConfigFactory}
import java.io.File
import java.util.Properties
import org.clapper.argot.ArgotParser
import org.json4s.jackson.JsonMethods._
import org.json4s.JsonDSL._
import org.json4s._
import Scalaz._
import scalaz._

// Whether the input stream contains enriched events or bad events
object StreamType extends Enumeration {
  type StreamType = Value

  val Good, Bad = Value
}

/**
  * Main entry point for the Elasticsearch sink
  */
object Main extends App {
  val parser = new ArgotParser(
    programName = generated.Settings.name,
    compactUsage = true,
    preUsage = Some(
      "%s: Version %s. Copyright (c) 2013, %s.".format(
        generated.Settings.name,
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
    .getOrElse(
      throw new RuntimeException("--config argument must be provided"))
    .resolve
    .getConfig("sink")

  lazy val streamType = ??? /*configValue.getString("stream-type") match {
    case "good" => StreamType.Good
    case "bad" => StreamType.Bad
    case _ =>
      throw new RuntimeException(
        """"stream-type" must be set to "good" or "bad"""")
  }*/

  val tracker =
    if (configValue.hasPath("monitoring.snowplow"))
      SnowplowTracking
        .initializeTracker(configValue.getConfig("monitoring.snowplow"))
        .some
    else None

  val rawConf = config.value
    .map(_.withFallback(defaultConfig))
    .getOrElse(throw new RuntimeException("--config option must be provided"))

  implicit val system = ActorSystem.create("kafka-elasticsearch-sink", rawConf)
  implicit val mat = ActorMaterializer()

  val finalConfig = convertConfig(configValue)

  lazy val goodSink = configValue.getString("sink.good") match {
    case "stdout" => ??? // Some(new StdouterrSink)
    case "elasticsearch" => None
  }

  lazy val badSink = configValue.getString("sink.bad") match {
    // case "stderr" => new StdouterrSink
    // case "none" => new NullSink
    case "kafka" =>
      val kafka = configValue.getConfig("kafka")
      val kafkaSink = kafka.getConfig("out")
      val kafkaSinkName = kafkaSink.getString("stream-name")
      // new KafkaSink(finalConfig.AWS_CREDENTIALS_PROVIDER,
      //               kinesisSinkEndpoint,
      //               kinesisSinkName,
      //               kinesisSinkShards)
      ???
  }

  lazy val executor = configValue.getString("source") match {
    // Read records from Kafka
    // case "kafka" =>
    //   new ElasticsearchSinkExecutor(streamType,
    //                                 documentIndex,
    //                                 documentType,
    //                                 finalConfig,
    //                                 goodSink,
    //                                 badSink,
    //                                 tracker,
    //                                 maxConnectionTime,
    //                                 clientType).success
    case _ => "Source must be set to 'kafka'".failure
  }

  import com.sksamuel.elastic4s.{ElasticClient, ElasticsearchClientUri}

  import org.elasticsearch.common.settings.Settings

  import akka.stream.scaladsl._
  import system.dispatcher

  val esClient: ElasticClient = {
    val settings = Settings.settingsBuilder
      .put("cluster.name", finalConfig.elasticCluster)
      .put("client.transport.sniff", false) // don't set sniff = true if local
      .build
    ElasticClient.transport(settings,
                            ElasticsearchClientUri(finalConfig.elasticEndpoint,
                                                   finalConfig.elasticPort))
  }

  import com.sksamuel.elastic4s.BulkCompatibleDefinition
  import com.sksamuel.elastic4s.ElasticDsl._
  import com.sksamuel.elastic4s.streams._
  import com.sksamuel.elastic4s.streams.ReactiveElastic._
  import com.sksamuel.elastic4s.source.JsonDocumentSource

  final class JsonRecordBuilder(documentIndex: String, documentType: String)
      extends RequestBuilder[JsonRecord] {
    val indexType = documentIndex / documentType

    override def request(record: JsonRecord): BulkCompatibleDefinition = {
      val doc = JsonDocumentSource(compact(render(record.json)))
      val q = index.into(indexType)
      record.id.map(q.id).getOrElse(q).doc(doc)
    }
  }

  implicit val builder = new JsonRecordBuilder(
    finalConfig.elasticDocumentIndex,
    finalConfig.elasticDocumentType)

  val esSink =
    Sink.fromSubscriber(new ReactiveElastic(esClient).subscriber[JsonRecord]())

  Kafka
    .source(finalConfig)
    .map { case (_, msg) => ElasticsearchTransformer.transform(msg) }
    .collect { case (_, Success(rec)) => rec }
    .runWith(esSink)

  /**
    * Builds a AppConfig from the "connector" field of the configuration HOCON
    *
    * @param connector The "connector" field of the configuration HOCON
    * @return A AppConfig
    */
  def convertConfig(connector: Config): AppConfig = {
    val elastic = connector.getConfig("elasticsearch")
    val kafka = connector.getConfig("kafka")

    AppConfig(
      kafkaHost = kafka.getString("host"),
      kafkaTopic = kafka.getString("topic"),
      kafkaGroup = kafka.getString("group"),
      elasticEndpoint = elastic.getString("endpoint"),
      elasticCluster = elastic.getString("cluster"),
      elasticPort = elastic.getInt("port"),
      elasticDocumentIndex = elastic.getString("document-index"),
      elasticDocumentType = elastic.getString("document-type")
    )
  }
}
