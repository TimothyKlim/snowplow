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

package com.snowplowanalytics.snowplow
package collectors
package scalastream
package sinks

// Java
import java.nio.ByteBuffer
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.ScheduledExecutorService

// Akka
import akka.actor.ActorSystem
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.NotUsed
import akka.stream.Materializer
import akka.stream.scaladsl._

// Kafka
import org.apache.kafka.common.serialization.{
  ByteArraySerializer,
  StringSerializer
}
import org.apache.kafka.clients.producer.ProducerRecord

// Config
import com.typesafe.config.Config

// Concurrent libraries
import scala.concurrent.{Future, Await, TimeoutException}
import scala.concurrent.duration._

// Logging
import org.slf4j.LoggerFactory

// Scala
import scala.util.{Success, Failure}
import scala.collection.JavaConverters._

// Snowplow
import CollectorPayload.thrift.model1.CollectorPayload

/**
  * Kafka Sink for the Scala collector.
  */
final class KafkaSink(config: CollectorConfig, inputType: InputType.InputType)(
    implicit sys: ActorSystem,
    mat: Materializer)
    extends AbstractSink {
  import log.{error, debug, info, trace}

  val MaxBytes = Long.MaxValue

  type Record = ProducerRecord[String, Array[Byte]]

  private val producerSettings =
    ProducerSettings(sys, new StringSerializer, new ByteArraySerializer)
      .withBootstrapServers(config.kafkaHost)

  private val consumer =
    Producer.plainSink[String, Array[Byte]](producerSettings)

  private val runnableGraph: RunnableGraph[Sink[Record, NotUsed]] =
    MergeHub.source[Record].to(consumer)

  private val toConsumer: Sink[Record, NotUsed] = runnableGraph.run()

  private val topicName = inputType match {
    case InputType.Good => config.kafkaTopicGoodName
    case InputType.Bad => config.kafkaTopicBadName
  }

  def storeRawEvents(events: List[Array[Byte]], key: String) = {
    Source(events)
      .map(e => new ProducerRecord[String, Array[Byte]](topicName, key, e))
      .runWith(toConsumer)
    Nil
  }
}
