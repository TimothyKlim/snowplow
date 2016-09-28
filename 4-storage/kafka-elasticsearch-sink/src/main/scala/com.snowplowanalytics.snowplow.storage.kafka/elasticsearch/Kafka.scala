package com.snowplowanalytics.snowplow.storage.kafka.elasticsearch

import akka.actor.ActorSystem
import akka.kafka.ConsumerMessage.{CommittableOffsetBatch, CommittableOffset}
import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.NotUsed
import akka.stream.Materializer
import akka.stream.scaladsl._
import org.apache.kafka.common.serialization.{
  ByteArrayDeserializer,
  StringDeserializer
}
import org.apache.kafka.clients.consumer._
import scala.concurrent.duration._

object Kafka {
  def source(config: AppConfig)(implicit sys: ActorSystem, mat: Materializer)
    : Source[(CommittableOffset, String), Consumer.Control] = {
    val consumerSettings =
      ConsumerSettings(sys, new StringDeserializer, new StringDeserializer)
        .withBootstrapServers(config.kafkaHost)
        .withGroupId(config.kafkaGroup)
        .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
    val topicName = Subscriptions.topics(config.kafkaTopic)
    Consumer
      .committableSource(consumerSettings, topicName)
      .map(msg => (msg.committableOffset, msg.record.value))
    // .groupedWithin(256, 1.second)
    // .map(
    //   _.foldRight((List.empty[CommittableOffset], List.empty[Array[Byte]])) {
    //     case (msg, (ofs, evs)) =>
    //       (msg.committableOffset :: ofs, msg.record.value :: evs)
    //   })
    // .mapAsync(3)(_.foldLeft(CommittableOffsetBatch.empty)(_.updated(_))
    //   .commitScaladsl())
    // .runWith(Sink.ignore)
  }
}
