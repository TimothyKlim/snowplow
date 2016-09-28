package com.snowplowanalytics.snowplow.storage

sealed trait StreamType

// Whether the input stream contains enriched events or bad events
object StreamType {
  final case object Good extends StreamType
  final case object Bad  extends StreamType
}
