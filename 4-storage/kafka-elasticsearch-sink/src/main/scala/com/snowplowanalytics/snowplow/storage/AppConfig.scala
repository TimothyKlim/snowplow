package com.snowplowanalytics.snowplow.storage

import com.typesafe.config.Config

final case class AppConfig(
    kafkaHost: String,
    kafkaTopic: String,
    kafkaGroup: String,
    elasticEndpoint: String,
    elasticCluster: String,
    elasticPort: Int,
    elasticDocumentIndex: String,
    elasticDocumentType: String
)

object AppConfig {
  def apply(config: Config): AppConfig = {
    val elastic = config.getConfig("elasticsearch")
    val kafka   = config.getConfig("kafka")

    AppConfig(
      kafkaHost = kafka.getString("host"),
      kafkaTopic = kafka.getString("topic"),
      kafkaGroup = kafka.getString("group"),
      elasticEndpoint = elastic.getString("endpoint"),
      elasticCluster = elastic.getString("cluster"),
      elasticPort = elastic.getInt("port"),
      elasticDocumentIndex = elastic.getString("document-index"),
      elasticDocumentType = elastic.getString("document-type")
    )
  }
}
