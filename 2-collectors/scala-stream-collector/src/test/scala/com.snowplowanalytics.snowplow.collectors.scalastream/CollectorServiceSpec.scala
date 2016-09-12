/*
 * Copyright (c) 2013-2016 Snowplow Analytics Ltd. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0, and
 * you may not use this file except in compliance with the Apache License
 * Version 2.0.  You may obtain a copy of the Apache License Version 2.0 at
 * http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Apache License Version 2.0 is distributed on an "AS
 * IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the Apache License Version 2.0 for the specific language
 * governing permissions and limitations there under.
 */
package com.snowplowanalytics.snowplow
package collectors
package scalastream

// Scala
import scala.collection.mutable.MutableList
import scala.collection.immutable

// Akka
import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl._, model._
import akka.http.scaladsl.model.ContentTypes.`application/json`
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.testkit.ScalatestRouteTest

// ScalaTest
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec}

// Config
import com.typesafe.config.{ConfigFactory, Config, ConfigException}

// Thrift
import org.apache.thrift.TDeserializer

// Snowplow
import sinks._
import CollectorPayload.thrift.model1.CollectorPayload

class CollectorServiceSpec
    extends WordSpec
    with Matchers
    with ScalatestRouteTest
    with HelpersSpec {
  val testConf: Config =
    ConfigFactory.parseString("""
collector {
  interface = "0.0.0.0"
  port = 8080

  production = true

  p3p {
    policyref = "/w3c/p3p.xml"
    CP = "NOI DSP COR NID PSA OUR IND COM NAV STA"
  }

  cookie {
    enabled = true
    expiration = 365 days
    name = sp
    domain = "test-domain.com"
  }

  sink {
    enabled = "test"

    kinesis {
      aws {
        access-key: "cpf"
        secret-key: "cpf"
      }
      stream {
        region: "us-east-1"
        good: "snowplow_collector_example"
        bad: "snowplow_collector_example"
      }
      buffer {
        byte-limit: 4000000 # 4MB
        record-limit: 500 # 500 records
        time-limit: 60000 # 1 minute
      }
      backoffPolicy {
        minBackoff: 3000 # 3 seconds
        maxBackoff: 600000 # 5 minutes
      }
    }
  }
}
""")
  val collectorConfig = new CollectorConfig(testConf)
  val sink = new TestSink
  val sinks = CollectorSinks(sink, sink)
  val collectorService = new CollectorService(collectorConfig, sinks)
  val thriftDeserializer = new TDeserializer

  // By default, spray will always add Remote-Address to every request
  // when running with the `spray.can.server.remote-address-header`
  // option. However, the testing does not read this option and a
  // remote address always needs to be set.
  private def collectorGet(uri: String,
                           cookie: Option[HttpCookiePair] = None,
                           remoteAddr: String = "127.0.0.1") = {
    val headers: MutableList[HttpHeader] =
      MutableList(`Remote-Address`(remoteAddress(remoteAddr)),
                  `Raw-Request-URI`(uri))
    cookie.foreach(headers += Cookie(_))
    Get(uri).withHeaders(headers.toList)
  }

  "Snowplow's Scala collector" should {
    "return an invisible pixel" in {
      collectorGet("/i") ~> collectorService.routes ~> check {
        responseAs[Array[Byte]] === ResponseHandler.pixel
      }
    }
    "return a cookie expiring at the correct time" in {
      collectorGet("/i") ~> collectorService.routes ~> check {
        headers should not be empty

        val httpCookies: immutable.Seq[HttpCookie] = headers.collect {
          case `Set-Cookie`(hc) => hc
        }
        httpCookies should not be empty

        // Assume we only return a single cookie.
        // If the collector is modified to return multiple cookies,
        // this will need to be changed.
        val httpCookie = httpCookies.head

        httpCookie.name should ===(collectorConfig.cookieName.get)
        httpCookie.domain shouldBe defined
        httpCookie.domain.get should ===(collectorConfig.cookieDomain.get)
        httpCookie.expires shouldBe defined
        val expiration = httpCookie.expires.get
        val offset = expiration.clicks - collectorConfig.cookieExpiration.get -
            DateTime.now.clicks
        offset.asInstanceOf[Int] should ===(0 +- 3600000) // 1 hour window.
      }
    }
    "return the same cookie as passed in" in {
      collectorGet(
        "/i",
        Some(HttpCookiePair(collectorConfig.cookieName.get, "UUID_Test"))) ~>
        collectorService.routes ~> check {
        val httpCookies: immutable.Seq[HttpCookie] = headers.collect {
          case `Set-Cookie`(hc) => hc
        }
        // Assume we only return a single cookie.
        // If the collector is modified to return multiple cookies,
        // this will need to be changed.
        val httpCookie = httpCookies(0)

        httpCookie.value should ===("UUID_Test")
      }
    }
    "return a P3P header" in {
      collectorGet("/i") ~> collectorService.routes ~> check {
        val p3pHeaders = headers.filter { h =>
          h.name.equals("P3P")
        }
        p3pHeaders.size should ===(1)
        val p3pHeader = p3pHeaders(0)

        val policyRef = collectorConfig.p3pPolicyRef
        val CP = collectorConfig.p3pCP
        p3pHeader.value should ===(
          "policyref=\"%s\", CP=\"%s\"".format(policyRef, CP))
      }
    }
    "store the expected event as a serialized Thrift object in the enabled sink" in {
      val payloadData = "param1=val1&param2=val2"
      val storedRecordBytes = collectorService.responseHandler
        .cookie(payloadData,
                null,
                None,
                None,
                "localhost",
                remoteAddress("127.0.0.1"),
                HttpRequest(),
                None,
                "/i",
                true)
        ._2

      val storedEvent = new CollectorPayload
      this.synchronized {
        thriftDeserializer.deserialize(storedEvent, storedRecordBytes.head)
      }

      storedEvent.timestamp should ===(DateTime.now.clicks +- 60000)
      storedEvent.encoding should ===("UTF-8")
      storedEvent.ipAddress should ===("127.0.0.1")
      storedEvent.collector should ===("ssc-0.8.0-test")
      storedEvent.path should ===("/i")
      storedEvent.querystring should ===(payloadData)
    }
    "report itself as healthy" in {
      collectorGet("/health") ~> collectorService.routes ~> check {
        response.status should ===(StatusCodes.OK)
      }
    }
  }
}
