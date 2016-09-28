package com.snowplowanalytics.snowplow.storage
package sink

import akka.NotUsed
import akka.kafka.ConsumerMessage.{CommittableOffset, CommittableOffsetBatch}
import akka.stream.scaladsl.Flow

sealed trait SinkType

object SinkType {
  final case object Kafka    extends SinkType
  final case object Elastic  extends SinkType
  final case object Postgres extends SinkType
  final case object Stdout   extends SinkType
}

trait StorageSink {
  val sinkType: SinkType

  val flow: Flow[(JsonRecord, Option[CommittableOffset]), CommittableOffsetBatch, NotUsed]
}
