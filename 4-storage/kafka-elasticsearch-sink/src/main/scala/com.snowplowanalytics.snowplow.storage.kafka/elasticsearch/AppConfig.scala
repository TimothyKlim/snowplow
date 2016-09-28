package com.snowplowanalytics.snowplow.storage.kafka.elasticsearch

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
