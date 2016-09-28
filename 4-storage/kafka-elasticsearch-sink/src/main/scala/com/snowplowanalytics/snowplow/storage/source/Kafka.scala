package com.snowplowanalytics.snowplow.storage
package source

import akka.NotUsed
import akka.actor.ActorSystem
import akka.kafka.ConsumerMessage.{CommittableOffset, CommittableOffsetBatch}
import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.stream.Materializer
import akka.stream.scaladsl.{Flow, Source, Sink}
import org.apache.kafka.clients.consumer._
import org.apache.kafka.common.serialization.StringDeserializer
import scala.concurrent.duration._

final class Kafka(config: AppConfig)(implicit sys: ActorSystem,
                                     mat: Materializer)
    extends StorageSource {
  val sourceType = SourceType.Kafka

  private val consumerSettings =
    ConsumerSettings(sys, new StringDeserializer, new StringDeserializer)
      .withBootstrapServers(config.kafkaHost)
      .withGroupId(config.kafkaGroup)
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

  private val topicName = Subscriptions.topics(config.kafkaTopic)

  val source: Source[(String, Option[CommittableOffset]), Consumer.Control] =
    Consumer
      .committableSource(consumerSettings, topicName)
      .map(msg => (msg.record.value, Some(msg.committableOffset)))

  val commitSink: Sink[CommittableOffsetBatch, NotUsed] =
    Flow[CommittableOffsetBatch]
      .mapAsync(3)(_.commitScaladsl())
      .to(Sink.ignore)
}
