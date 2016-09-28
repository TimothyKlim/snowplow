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
package com.snowplowanalytics.snowplow.storage.kafka.elasticsearch

// Java
import java.nio.charset.StandardCharsets.UTF_8

// Scalaz
import scalaz._
import Scalaz._

// TODO consider giving BadEventTransformer its own types

/**
  * Class to convert bad events to ElasticsearchObjects
  *
  * @param the elasticsearch index name
  * @param the elasticsearch index type
  */
// class BadEventTransformer(documentIndex: String, documentType: String)
//     extends StdinTransformer {
//
//   def consumeLine(line: String): ValidatedRecord = ???
//
//   /**
//     * Convert an Kafka record to a JSON string
//     *
//     * @param record Byte array representation of a bad row string
//     * @return JsonRecord containing JSON string for the event and no event_id
//     */
//   def toClass(recordString: String): ValidatedRecord =
//     (recordString, JsonRecord(recordString, None).success)
// }
