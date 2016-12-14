package com.snowplowanalytics.snowplow.storage
package sink

import akka.http.scaladsl.util.FastFuture._
import akka.kafka.ConsumerMessage.{CommittableOffset, CommittableOffsetBatch}
import akka.stream.scaladsl._
import com.sksamuel.elastic4s.bulk.BulkCompatibleDefinition
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.{ElasticClient, ElasticsearchClientUri}
import com.typesafe.scalalogging.LazyLogging
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.indices.IndexTemplateAlreadyExistsException
import org.json4s.jackson.JsonMethods._
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

final class Elastic(config: ElasticConfig)(implicit ec: ExecutionContext)
    extends StorageSink
    with LazyLogging {
  val sinkType = SinkType.Elastic

  private val indexName     = config.documentIndex
  private val indexTypeName = config.documentType
  private val indexType     = indexName / indexTypeName

  private val client: ElasticClient = {
    val settings = Settings.builder
      .put("cluster.name", config.cluster)
      .put("client.transport.sniff", false)
      .build
    val client =
      ElasticsearchClientUri(config.endpoint, config.port)
    ElasticClient.transport(settings, client)
  }

  private def mapRecord(record: JsonRecord): BulkCompatibleDefinition = {
    val doc = compact(render(record.json))
    val q   = indexInto(indexType)
    record.id.map(q.id).getOrElse(q).doc(doc)
  }

  private lazy val createElasticIndex: Future[_] =
    client.execute(createIndex(indexName).mappings(mapping(indexTypeName))).recover {
      case e: IndexTemplateAlreadyExistsException => ()
      case e: Throwable                           => logger.error(e.getMessage, e)
    }

  val flow =
    Flow[(JsonRecord, Option[CommittableOffset])].groupedWithin(100, 250.millis).mapAsync(6) {
      xs =>
        val (records, offset) =
          xs.foldLeft((List.empty[BulkCompatibleDefinition], CommittableOffsetBatch.empty)) {
            case ((evs, batch), (rec, offset)) =>
              (mapRecord(rec) :: evs, offset.map(batch.updated).getOrElse(batch))
          }

        def insertRecords() = insert(records, offset)

        if (createElasticIndex.isCompleted) insertRecords()
        else createElasticIndex.flatMap(_ => insertRecords())
    }

  private def insert(records: List[BulkCompatibleDefinition], offset: CommittableOffsetBatch) =
    client.execute(bulk(records)).fast.map(_ => offset).recover {
      case e: Throwable =>
        logger.error(e.getMessage, e)
        offset
    }
}
