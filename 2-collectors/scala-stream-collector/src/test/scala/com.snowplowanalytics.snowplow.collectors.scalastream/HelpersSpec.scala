package com.snowplowanalytics.snowplow
package collectors
package scalastream

import akka.http.scaladsl.model.RemoteAddress
import java.net.InetAddress

trait HelpersSpec {
  protected def remoteAddress(ip: String) =
    RemoteAddress(InetAddress.getByName(ip))
}
