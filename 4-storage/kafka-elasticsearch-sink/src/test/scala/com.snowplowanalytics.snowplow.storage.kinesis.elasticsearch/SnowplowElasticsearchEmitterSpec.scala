/**
  * Copyright (c) 2014-2016 Snowplow Analytics Ltd. All rights reserved.
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
package com.snowplowanalytics.snowplow.storage

// Java
import java.util.Properties

// Scala
import scala.collection.JavaConversions._
import scala.collection.JavaConverters._

// Scalaz
import scalaz._
import Scalaz._

// json4s
import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.json4s.JsonDSL._

// Specs2
import org.specs2.mutable.Specification
import org.specs2.scalaz.ValidationMatchers

/**
  * Tests Shredder
  */
class SnowplowElasticsearchEmitterSpec extends Specification with ValidationMatchers {

  "The emit method" should {
    "return all invalid records" in {

      val eem = new SnowplowElasticsearchEmitter(Some(new StdouterrSink), new StdouterrSink)

      val validInput: ValidatedRecord   = "good" -> JObject(Nil).success
      val invalidInput: ValidatedRecord = "bad"  -> List("malformed event").failure

      val input = List(validInput, invalidInput)

      val bmb = new BasicMemoryBuffer[ValidatedRecord](kcc, input)
      val ub  = new UnmodifiableBuffer[ValidatedRecord](bmb)

      val actual = eem.emit(ub)

      actual must_== List(invalidInput).asJava
    }
  }

}
