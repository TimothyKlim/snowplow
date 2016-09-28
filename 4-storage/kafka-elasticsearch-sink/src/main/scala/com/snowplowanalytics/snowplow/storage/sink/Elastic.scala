package com.snowplowanalytics.snowplow.storage
package sink

import akka.http.scaladsl.util.FastFuture._
import akka.kafka.ConsumerMessage.{CommittableOffset, CommittableOffsetBatch}
import akka.stream.scaladsl._
import com.sksamuel.elastic4s.BulkCompatibleDefinition
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.source.JsonDocumentSource
import com.sksamuel.elastic4s.{ElasticClient, ElasticsearchClientUri}
import com.typesafe.scalalogging.LazyLogging
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.common.settings.Settings
import org.json4s.jackson.JsonMethods._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext

final class Elastic(config: AppConfig)(implicit ec: ExecutionContext)
    extends StorageSink
    with LazyLogging {
  val sinkType = SinkType.Elastic

  private val indexName     = config.elasticDocumentIndex
  private val indexTypeName = config.elasticDocumentType
  private val indexType     = indexName / indexTypeName

  private val client: ElasticClient = {
    val settings = Settings.settingsBuilder
      .put("cluster.name", config.elasticCluster)
      .put("client.transport.sniff", false)
      .build
    val client =
      ElasticsearchClientUri(config.elasticEndpoint, config.elasticPort)
    ElasticClient.transport(settings, client)
  }

  private def mapRecord(record: JsonRecord): BulkCompatibleDefinition = {
    val doc = JsonDocumentSource(compact(render(record.json)))
    val q   = index.into(indexType)
    record.id.map(q.id).getOrElse(q).doc(doc)
  }

  private lazy val createIndex =
    client.execute(create.index(indexName).mappings(mapping(indexTypeName))).recover {
      case e: Throwable =>
        logger.error(e.getMessage, e)
    }

  val flow =
    Flow[(JsonRecord, Option[CommittableOffset])].groupedWithin(100, 1.second).mapAsync(3) { xs =>
      println(s"xs: $xs")
      val (records, offset) =
        xs.foldLeft((List.empty[BulkCompatibleDefinition], CommittableOffsetBatch.empty)) {
          case ((evs, batch), (rec, offset)) =>
            (mapRecord(rec) :: evs, offset.map(batch.updated).getOrElse(batch))
        }

      def insertRecords() = insert(records, offset)

      if (createIndex.isCompleted) insertRecords()
      else createIndex.flatMap(_ => insertRecords())
    }

  private def insert(records: List[BulkCompatibleDefinition], offset: CommittableOffsetBatch) =
    client.execute(bulk(records)).fast.map(_ => offset).recover {
      case e: Throwable =>
        logger.error(e.getMessage, e)
        offset
    }
}
