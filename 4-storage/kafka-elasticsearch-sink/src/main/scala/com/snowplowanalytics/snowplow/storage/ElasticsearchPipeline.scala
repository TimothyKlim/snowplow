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
package com.snowplowanalytics.snowplow
package storage

// This project
import StreamType._

// Tracker
import scalatracker.Tracker

/**
  * ElasticsearchPipeline class sets up the Emitter/Buffer/Transformer/Filter
  *
  * @param streamType the type of stream, good/bad
  * @param documentIndex the elasticsearch index name
  * @param documentType the elasticsearch index type
  * @param goodSink the configured GoodSink
  * @param badSink the configured BadSink
  * @param tracker a Tracker instance
  * @param maxConnectionTime the maximum amount of time
  *        we can attempt to send to elasticsearch
  * @param elasticsearchClientType The type of ES Client to use
  */
class ElasticsearchPipeline(
    streamType: StreamType,
    documentIndex: String,
    documentType: String,
    // goodSink: Option[ISink],
    // badSink: ISink,
    tracker: Option[Tracker] = None,
    maxConnectionTime: Long,
    elasticsearchClientType: String
) {

  // override def getEmitter(
  //     configuration: KafkaConnectorConfiguration): IEmitter[ValidatedRecord] =
  //   new SnowplowElasticsearchEmitter(configuration,
  //                                    goodSink,
  //                                    badSink,
  //                                    tracker,
  //                                    maxConnectionTime,
  //                                    elasticsearchClientType)
  //
  // override def getBuffer(configuration: KafkaConnectorConfiguration) =
  //   new BasicMemoryBuffer[ValidatedRecord](configuration)
  //
  // override def getTransformer(c: KafkaConnectorConfiguration) =
  //   streamType match {
  //     case Good =>
  //       new Transformer(documentIndex, documentType)
  //     case Bad => new BadEventTransformer(documentIndex, documentType)
  //   }
  //
  // override def getFilter(c: KafkaConnectorConfiguration) =
  //   new AllPassFilter[ValidatedRecord]()
}
