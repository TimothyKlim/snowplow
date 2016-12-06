package com.snowplowanalytics.snowplow.storage
package sink

import akka.kafka.ConsumerMessage.{CommittableOffset, CommittableOffsetBatch}
import akka.stream.scaladsl._
import com.typesafe.scalalogging.LazyLogging
import doobie.imports._
import io.fcomb.db.Migration
import java.sql.Timestamp
import java.time.{OffsetDateTime, ZoneId}
import java.util.Date
import org.json4s.jackson.JsonMethods._
import org.json4s._, JsonDSL._
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Promise}
import scala.util.{Failure, Success, Try}
import _root_.scalaz._
import _root_.scalaz.Scalaz._
import _root_.scalaz.concurrent.Task

object DoobieImplicits {
  final implicit val offsetDateTimeMeta: Meta[OffsetDateTime] =
    Meta[String].nxmap(OffsetDateTime.parse(_), _.toString)
}
import DoobieImplicits._

final case class PostgresEvent(
    app_id: Option[String],
    platform: Option[String],
    etl_tstamp: Option[OffsetDateTime],
    collector_tstamp: OffsetDateTime,
    dvce_created_tstamp: Option[OffsetDateTime],
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
    dvce_sent_tstamp: Option[OffsetDateTime],
    refr_domain_userid: Option[String],
    refr_dvce_tstamp: Option[OffsetDateTime],
    domain_sessionid: Option[String],
    derived_tstamp: Option[OffsetDateTime],
    event_vendor: Option[String],
    event_name: Option[String],
    event_format: Option[String],
    event_version: Option[String],
    event_fingerprint: Option[String],
    true_tstamp: Option[OffsetDateTime]
)

object PostgresEvent {
  val insert =
    Update[PostgresEvent](
      """
      INSERT INTO suppliers (app_id, platform, etl_tstamp, collector_tstamp, dvce_created_tstamp, event, event_id, txn_id, name_tracker, v_tracker, v_collector, v_etl, user_id, user_ipaddress, user_fingerprint, domain_userid, domain_sessionidx, network_userid, geo_country, geo_region, geo_city, geo_zipcode, geo_latitude, geo_longitude, geo_region_name, ip_isp, ip_organization, ip_domain, ip_netspeed, page_url, page_title, page_referrer, page_urlscheme, page_urlhost, page_urlport, page_urlpath, page_urlquery, page_urlfragment, refr_urlscheme, refr_urlhost, refr_urlport, refr_urlpath, refr_urlquery, refr_urlfragment, refr_medium, refr_source, refr_term, mkt_medium, mkt_source, mkt_term, mkt_content, mkt_campaign, se_category, se_action, se_label, se_property, se_value, tr_orderid, tr_affiliation, tr_total, tr_tax, tr_shipping, tr_city, tr_state, tr_country, ti_orderid, ti_sku, ti_name, ti_category, ti_price, ti_quantity, pp_xoffset_min, pp_xoffset_max, pp_yoffset_min, pp_yoffset_max, useragent, br_name, br_family, br_version, br_type, br_renderengine, br_lang, br_features_pdf, br_features_flash, br_features_java, br_features_director, br_features_quicktime, br_features_realplayer, br_features_windowsmedia, br_features_gears, br_features_silverlight, br_cookies, br_colordepth, br_viewwidth, br_viewheight, os_name, os_family, os_manufacturer, os_timezone, dvce_type, dvce_ismobile, dvce_screenwidth, dvce_screenheight, doc_charset, doc_width, doc_height, tr_currency, tr_total_base, tr_tax_base, tr_shipping_base, ti_currency, ti_price_base, base_currency, geo_timezone, mkt_clickid, mkt_network, etl_tags, dvce_sent_tstamp, refr_domain_userid, refr_dvce_tstamp, domain_sessionid, derived_tstamp, event_vendor, event_name, event_format, event_version, event_fingerprint, true_tstamp)
      VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
      ON CONFLICT DO NOTHING
      """)
}

final case object JOffsetDateTimeSerializer
    extends CustomSerializer[OffsetDateTime](_ =>
      ({
        case JString(s)             => OffsetDateTime.parse(s)
        case JNull                  => null
      }, { case odt: OffsetDateTime => JString(odt.toString) }))

final class Postgres(config: PostgresConfig)(implicit ec: ExecutionContext)
    extends StorageSink
    with LazyLogging {
  final val sinkType = SinkType.Postgres

  private final val xa: Transactor[Task] =
    DriverManagerTransactor("org.postgresql.Driver", config.url, config.user, config.password)

  private final lazy val migration = Migration.run(config.url, config.user, config.password)

  final implicit val formats = DefaultFormats ++ List(JOffsetDateTimeSerializer)

  final val flow =
    Flow[(JsonRecord, Option[CommittableOffset])].groupedWithin(100, 250.millis).mapAsync(6) {
      xs =>
        println(s"xs: $xs")

        val (records, offset) =
          xs.foldLeft((List.empty[PostgresEvent], CommittableOffsetBatch.empty)) {
            case ((evs, batch), (rec, offset)) =>
              val offsetBatch = offset.map(batch.updated).getOrElse(batch)
              Try(rec.json.extract[PostgresEvent]) match {
                case Success(event) => (event :: evs, offsetBatch)
                case Failure(e) =>
                  logger.error(e.toString)
                  (evs, offsetBatch)
              }
          }

        println(s"records: $records")

        def insertRecords() = {
          val p = Promise[CommittableOffsetBatch]()
          PostgresEvent.insert.updateMany(records).transact(xa).unsafePerformAsync { res =>
            p.success(offset)
            res match {
              case -\/(e) => logger.error(e.getMessage, e)
              case _      =>
            }
          }
          p.future
        }

        if (migration.isCompleted) insertRecords()
        else migration.flatMap(_ => insertRecords())
    }
}
