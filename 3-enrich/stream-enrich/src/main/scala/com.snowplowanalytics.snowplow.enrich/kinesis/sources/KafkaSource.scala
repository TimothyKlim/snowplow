/*
 * Copyright (c) 2013-2016 Snowplow Analytics Ltd.
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

package com.snowplowanalytics
package snowplow
package enrich
package kinesis
package sources

// Akka
import akka.actor.ActorSystem
import akka.kafka.ConsumerMessage.{CommittableOffsetBatch, CommittableOffset}
import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.NotUsed
import akka.stream.Materializer
import akka.stream.scaladsl._

// Kafka
import org.apache.kafka.common.serialization.{
  ByteArrayDeserializer,
  StringDeserializer
}
import org.apache.kafka.clients.consumer._

// Iglu
import iglu.client.Resolver

// Snowplow events and enrichment
import common.enrichments.EnrichmentRegistry

// Tracker
import com.snowplowanalytics.snowplow.scalatracker.Tracker

import scala.concurrent.duration._

final class KafkaSource(
    config: KinesisEnrichConfig,
    igluResolver: Resolver,
    enrichmentRegistry: EnrichmentRegistry,
    tracker: Option[Tracker])(implicit sys: ActorSystem, mat: Materializer)
    extends AbstractSource(config, igluResolver, enrichmentRegistry, tracker) {

  private val consumerSettings =
    ConsumerSettings(sys, new StringDeserializer, new ByteArrayDeserializer)
      .withBootstrapServers(config.kafkaHost)
      .withGroupId(config.kafkaGroup)
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

  private val topicName = Subscriptions.topics(config.rawInStream)

  private val consumer =
    Consumer.committableSource(consumerSettings, topicName)

  def run() =
    consumer
      .groupedWithin(256, 1.second)
      .map { group =>
        val (offsets, events) =
          group.foldRight(
            (List.empty[CommittableOffset], List.empty[Array[Byte]])) {
            case (msg, (ofs, evs)) =>
              (msg.committableOffset :: ofs, msg.record.value :: evs)
          }
        enrichAndStoreEvents(events)
        offsets
      }
      .mapAsync(3)(_.foldLeft(CommittableOffsetBatch.empty)(_.updated(_))
        .commitScaladsl())
      .runWith(Sink.ignore)
}
