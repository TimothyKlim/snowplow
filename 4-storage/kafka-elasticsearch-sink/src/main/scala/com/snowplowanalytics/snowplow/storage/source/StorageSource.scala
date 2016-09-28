package com.snowplowanalytics.snowplow.storage
package source

import akka.kafka.ConsumerMessage.{CommittableOffset, CommittableOffsetBatch}
import akka.kafka.scaladsl.Consumer
import akka.NotUsed
import akka.stream.scaladsl.{Flow, Sink, Source}

sealed trait SourceType

object SourceType {
  final case object Kafka extends SourceType
  final case object Stdin extends SourceType
}

trait StorageSource {
  val sourceType: SourceType

  val source: Source[(String, Option[CommittableOffset]), Consumer.Control]

  val commitSink: Sink[CommittableOffsetBatch, NotUsed]
}
