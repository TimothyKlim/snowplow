package com.snowplowanalytics.snowplow.enrich
package kinesis
package sinks

// Akka
import akka.actor.ActorSystem
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.NotUsed
import akka.stream.Materializer
import akka.stream.scaladsl._

// Kafka
import org.apache.kafka.common.serialization.{ByteArraySerializer, StringSerializer}
import org.apache.kafka.clients.producer.ProducerRecord

final class KafkaSink(config: KinesisEnrichConfig,
                      inputType: InputType.InputType)(implicit sys: ActorSystem, mat: Materializer)
    extends ISink {

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
    case InputType.Good => config.enrichedOutStream
    case InputType.Bad  => config.badOutStream
  }

  /**
    * Side-effecting function to store the EnrichedEvent
    * to the given output stream.
    *
    * EnrichedEvent takes the form of a tab-delimited
    * String until such time as https://github.com/snowplow/snowplow/issues/211
    * is implemented.
    *
    * @param events Sequence of enriched events and (unused) partition keys
    * @return Whether to checkpoint
    */
  def storeEnrichedEvents(events: List[(String, String)]): Boolean = {
    Source(events)
      .map {
        case (e, key) =>
          println(new String(e.getBytes))
          new ProducerRecord[String, Array[Byte]](topicName, key, e.getBytes)
      }
      .runWith(toConsumer)
    !events.isEmpty
  }

  def flush(): Unit = ()
}
