/*
 * Copyright (c) 2013-2014 Snowplow Analytics Ltd. All rights reserved.
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

// Akka
import akka.actor.{Actor, ActorRefFactory}
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.pattern.ask
import akka.util.Timeout

// Scala
import scala.concurrent.duration._

// Snowplow
import sinks._
import utils.SplitBatch

/**
  * Companion object for the CollectorService class
  */
object CollectorService {
  private val querystringExtractor = "^[^?]*\\?([^#]*)(?:#.*)?$".r
}

final class CollectorService(collectorConfig: CollectorConfig, sinks: CollectorSinks) {
  val responseHandler = new ResponseHandler(collectorConfig, sinks)
  val cookieName      = collectorConfig.cookieName

  // format: OFF
  val routes = {
    path(Segment / Segment) { (path1, path2) =>
      getRequestDetails(cookieName) {
        case (reqCookie, userAgent, refererURI, rawRequest, host, ip, request) =>
          post {
            entity(as[String]) { body =>
              complete(responseHandler.cookie(null, body, reqCookie, userAgent, host, ip, request, refererURI,
                "/" + path1 + "/" + path2, false)._1)
            }
          } ~
          get {
            val q = rawRequest match {
              case CollectorService.querystringExtractor(qs) => qs
              case _ => ""
            }
            complete(responseHandler.cookie(q, null, reqCookie, userAgent, host, ip, request, refererURI,
              "/" + path1 + "/" + path2, true)._1)
          }
      }
    } ~
    path("""ice\.png""".r | "i".r) { path =>
      get {
        getRequestDetails(cookieName) {
          case (reqCookie, userAgent, refererURI, rawRequest, host, ip, request) =>
            val q = rawRequest match {
              case CollectorService.querystringExtractor(qs) => qs
              case _ => ""
            }
            complete(responseHandler.cookie(q, null, reqCookie, userAgent, host, ip, request, refererURI,
              "/" + path, true)._1)
        }
      }
    } ~
    path("health".r)(_ => get(complete(responseHandler.healthy))) ~
    options(extractRequest(req => complete(responseHandler.preflightResponse(req)))) ~
    path("""crossdomain\.xml""".r)(_ => get(complete(responseHandler.flashCrossDomainPolicy))) ~
    complete(HttpResponse(StatusCodes.NotFound))
  }
  // format: ON

  private def getRequestDetails(cookieName: Option[String]): Directive1[(Option[HttpCookiePair],
                                                                         Option[String],
                                                                         Option[String],
                                                                         String,
                                                                         String,
                                                                         RemoteAddress,
                                                                         HttpRequest)] =
    cookieIfWanted(cookieName).flatMap { reqCookie =>
      optionalHeaderValueByName("User-Agent").flatMap { userAgent =>
        optionalHeaderValueByName("Referer").flatMap { refererURI =>
          headerValueByName("Raw-Request-URI").flatMap { rawRequest =>
            extractHost.flatMap { host =>
              extractClientIP.flatMap { ip =>
                extractRequest.flatMap { request =>
                  provide((reqCookie, userAgent, refererURI, rawRequest, host, ip, request))
                }
              }
            }
          }
        }
      }
    }

  /**
    * Directive to extract a cookie if a cookie name is specified and if such a cookie exists
    *
    * @param name Optionally configured cookie name
    * @return Directive1[Option[HttpCookie]]
    */
  def cookieIfWanted(name: Option[String]): Directive1[Option[HttpCookiePair]] =
    name match {
      case Some(n) => optionalCookie(n)
      case None    => provide(None)
    }
}
