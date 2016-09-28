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

// Java
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets.UTF_8

// Scala
import scala.util.Random

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

// Concurrent libraries
import scala.concurrent.{Future, Await, TimeoutException}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.util.{Success, Failure}

// Logging
import org.slf4j.LoggerFactory

/**
  * Kafka Sink
  *
  * @param endpoint Kafka stream endpoint
  * @param topic Kafka stream name
  * @param config Configuration for the Kafka stream
  */
final class KafkaSink(config: AppConfig)(implicit sys: ActorSystem,
                                         mat: Materializer) {
  private lazy val log = LoggerFactory.getLogger(getClass())
  import log.{error, debug, info, trace}

  type Record = ProducerRecord[String, Array[Byte]]

  private val producerSettings =
    ProducerSettings(sys, new StringSerializer, new ByteArraySerializer)
      .withBootstrapServers(config.kafkaHost)

  private val consumer =
    Producer.plainSink[String, Array[Byte]](producerSettings)

  private val runnableGraph: RunnableGraph[Sink[Record, NotUsed]] =
    MergeHub.source[Record].to(consumer)

  private val toConsumer: Sink[Record, NotUsed] = runnableGraph.run()

  /**
    * Write a record to the Kafka stream
    *
    * @param output The string record to write
    * @param key A hash of the key determines to which shard the
    *            record is assigned. Defaults to a random string.
    * @param good Unused parameter which exists to extend ISink
    */
  // def store(output: String, key: Option[String], good: Boolean) {
  //   val putData = for {
  //     p <- enrichedStream.put(
  //       ByteBuffer.wrap(output.getBytes(UTF_8)),
  //       key.getOrElse(Random.nextInt.toString)
  //     )
  //   } yield p
  //
  //   putData onComplete {
  //     case Success(result) => {
  //       info(s"Writing successful")
  //       info(s"  + ShardId: ${result.shardId}")
  //       info(s"  + SequenceNumber: ${result.sequenceNumber}")
  //     }
  //     case Failure(f) => {
  //       error(s"Writing failed.")
  //       error(s"  + " + f.getMessage)
  //     }
  //   }
  // }
}
