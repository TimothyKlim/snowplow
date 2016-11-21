package com.snowplowanalytics.snowplow.storage
package sink

import akka.http.scaladsl.util.FastFuture._
import akka.kafka.ConsumerMessage.{CommittableOffset, CommittableOffsetBatch}
import akka.stream.scaladsl._
import com.typesafe.scalalogging.LazyLogging
import io.fcomb.db.Migration
import java.sql.Timestamp
import java.time.{LocalDateTime, ZoneId}
import java.util.Date
import org.json4s.jackson.JsonMethods._
import org.json4s._, JsonDSL._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success, Try}

final case class PostgresEvent(
    app_id: Option[String],
    platform: Option[String],
    etl_tstamp: Option[LocalDateTime],
    collector_tstamp: LocalDateTime,
    dvce_created_tstamp: Option[LocalDateTime],
    event: Option[String],
    event_id: String,
    txn_id: Option[Int],
    name_tracker: Option[String],
    v_tracker: Option[String],
    v_collector: String,
    v_etl: String,
    user_id: Option[String],
    user_ipaddress: Option[String],
    user_fingerprint: Option[String],
    domain_userid: Option[String],
    domain_sessionidx: Option[Int],
    network_userid: Option[String],
    geo_country: Option[String],
    geo_region: Option[String],
    geo_city: Option[String],
    geo_zipcode: Option[String],
    geo_latitude: Option[Double],
    geo_longitude: Option[Double],
    geo_region_name: Option[String],
    ip_isp: Option[String],
    ip_organization: Option[String],
    ip_domain: Option[String],
    ip_netspeed: Option[String],
    page_url: Option[String],
    page_title: Option[String],
    page_referrer: Option[String],
    page_urlscheme: Option[String],
    page_urlhost: Option[String],
    page_urlport: Option[Int],
    page_urlpath: Option[String],
    page_urlquery: Option[String],
    page_urlfragment: Option[String],
    refr_urlscheme: Option[String],
    refr_urlhost: Option[String],
    refr_urlport: Option[Int],
    refr_urlpath: Option[String],
    refr_urlquery: Option[String],
    refr_urlfragment: Option[String],
    refr_medium: Option[String],
    refr_source: Option[String],
    refr_term: Option[String],
    mkt_medium: Option[String],
    mkt_source: Option[String],
    mkt_term: Option[String],
    mkt_content: Option[String],
    mkt_campaign: Option[String],
    se_category: Option[String],
    se_action: Option[String],
    se_label: Option[String],
    se_property: Option[String],
    se_value: Option[Double],
    tr_orderid: Option[String],
    tr_affiliation: Option[String],
    tr_total: Option[Double],
    tr_tax: Option[Double],
    tr_shipping: Option[Double],
    tr_city: Option[String],
    tr_state: Option[String],
    tr_country: Option[String],
    ti_orderid: Option[String],
    ti_sku: Option[String],
    ti_name: Option[String],
    ti_category: Option[String],
    ti_price: Option[Double],
    ti_quantity: Option[Int],
    pp_xoffset_min: Option[Int],
    pp_xoffset_max: Option[Int],
    pp_yoffset_min: Option[Int],
    pp_yoffset_max: Option[Int],
    useragent: Option[String],
    br_name: Option[String],
    br_family: Option[String],
    br_version: Option[String],
    br_type: Option[String],
    br_renderengine: Option[String],
    br_lang: Option[String],
    br_features_pdf: Option[Boolean],
    br_features_flash: Option[Boolean],
    br_features_java: Option[Boolean],
    br_features_director: Option[Boolean],
    br_features_quicktime: Option[Boolean],
    br_features_realplayer: Option[Boolean],
    br_features_windowsmedia: Option[Boolean],
    br_features_gears: Option[Boolean],
    br_features_silverlight: Option[Boolean],
    br_cookies: Option[Boolean],
    br_colordepth: Option[String],
    br_viewwidth: Option[Int],
    br_viewheight: Option[Int],
    os_name: Option[String],
    os_family: Option[String],
    os_manufacturer: Option[String],
    os_timezone: Option[String],
    dvce_type: Option[String],
    dvce_ismobile: Option[Boolean],
    dvce_screenwidth: Option[Int],
    dvce_screenheight: Option[Int],
    doc_charset: Option[String],
    doc_width: Option[Int],
    doc_height: Option[Int],
    tr_currency: Option[String],
    tr_total_base: Option[Double],
    tr_tax_base: Option[Double],
    tr_shipping_base: Option[Double],
    ti_currency: Option[String],
    ti_price_base: Option[Double],
    base_currency: Option[String],
    geo_timezone: Option[String],
    mkt_clickid: Option[String],
    mkt_network: Option[String],
    etl_tags: Option[String],
    dvce_sent_tstamp: Option[LocalDateTime],
    refr_domain_userid: Option[String],
    refr_dvce_tstamp: Option[LocalDateTime],
    domain_sessionid: Option[String],
    derived_tstamp: Option[LocalDateTime],
    event_vendor: Option[String],
    event_name: Option[String],
    event_format: Option[String],
    event_version: Option[String],
    event_fingerprint: Option[String],
    true_tstamp: Option[LocalDateTime]
)

final case object JLocalDateTimeSerializer
    extends CustomSerializer[LocalDateTime](
      format =>
        (
          {
            case JString(s) =>
              val date = format.dateFormat
                .parse(s)
                .map(d => new Date(d.getTime))
                .getOrElse(throw new MappingException(s"Invalid date format $s"))
              LocalDateTime.ofInstant(date.toInstant, ZoneId.systemDefault)
            case JNull => null
          }, {
            case ldt: LocalDateTime =>
              val instant = ldt.atZone(ZoneId.systemDefault).toInstant
              JString(format.dateFormat.format(Date.from(instant)))
          }
      ))

final class Postgres(config: PostgresConfig)(implicit ec: ExecutionContext)
    extends StorageSink
    with LazyLogging {
  val sinkType = SinkType.Postgres

  // private lazy val db = Database.forConfig("", Config.jdbcConfigSlick)

  private lazy val migration = Migration.run(config.url, config.user, config.password)

  implicit val formats = DefaultFormats ++ List(JLocalDateTimeSerializer)

  val flow =
    Flow[(JsonRecord, Option[CommittableOffset])].groupedWithin(100, 250.millis).mapAsync(6) {
      xs =>
        val (records, offset) =
          xs.foldLeft((List.empty[Seq[(String, Any)]], CommittableOffsetBatch.empty)) {
            case ((evs, batch), (rec, offset)) =>
              val offsetBatch = offset.map(batch.updated).getOrElse(batch)
              Try(rec.json.extract[PostgresEvent]) match {
                case Success(event) => (evs, offsetBatch)
                case Failure(e) =>
                  println(e)
                  (evs, offsetBatch)
              }
          }

        println(s"records: $records")

        // def insertRecords() = insert(records, offset)
        //
        // if (createIndex.isCompleted) insertRecords()
        // else createIndex.flatMap(_ => insertRecords())
        ???
    }
}
