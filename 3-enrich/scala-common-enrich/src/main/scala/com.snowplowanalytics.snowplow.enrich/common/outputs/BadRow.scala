/*
 * Copyright (c) 2014-2015 Snowplow Analytics Ltd. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package com.snowplowanalytics
package snowplow
package enrich
package common
package outputs

import java.time.{Instant, ZoneOffset}

// Scalaz
import scalaz._
import Scalaz._

// json4s
import org.json4s._
import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods._

// Iglu Scala Client
import iglu.client.ProcessingMessageNel
import iglu.client.validation.ProcessingMessageMethods._

/**
  * Alternate BadRow constructors
  */
object BadRow {

  /**
    * Constructor using Strings instead of ProcessingMessages
    *
    * @param line
    * @param errors
    * @return a BadRow
    */
  def apply(line: String, errors: NonEmptyList[String]): BadRow =
    BadRow(line, errors.map(_.toProcessingMessage), System.currentTimeMillis())

  /**
    * For rows which are so too long to send to Kinesis and so cannot contain their own original line
    *
    * @param line
    * @param errors
    * @param tstamp
    * @return a BadRow
    */
  def oversizedRow(size: Long,
                   errors: NonEmptyList[String],
                   tstamp: Long = System.currentTimeMillis()): String =
    compact(
      ("size" -> size) ~
        ("errors" -> errors.toList.map(e =>
          fromJsonNode(e.toProcessingMessage.asJson))) ~
        ("failure_tstamp" -> tstamp)
    )
}

/**
  * Models our report on a bad row. Consists of:
  * 1. Our original input line (which was meant
  *    to be a Snowplow enriched event)
  * 2. A non-empty list of our Validation errors
  * 3. A timestamp
  */
case class BadRow(line: String,
                  errors: ProcessingMessageNel,
                  tstamp: Long = System.currentTimeMillis()) {

  /**
    * Converts a TypeHierarchy into a JSON containing
    * each element.
    *
    * @return the TypeHierarchy as a json4s JValue
    */
  def toJValue: JValue =
    ("line" -> line) ~
      ("errors" -> errors.toList.map(e => fromJsonNode(e.asJson))) ~
      ("failure_tstamp" -> getTimestamp(tstamp))

  /**
    * Converts our BadRow into a single JSON encapsulating
    * both the input line and errors.
    *
    * @return this BadRow as a compact stringified JSON
    */
  def toCompactJson: String =
    compact(toJValue)

  /**
    * Returns an ISO valid timestamp
    *
    * @param tstamp The Timestamp to convert
    * @return the formatted Timestamp
    */
  private def getTimestamp(tstamp: Long): String =
    Instant.ofEpochSecond(tstamp).atOffset(ZoneOffset.UTC).toString()
}
