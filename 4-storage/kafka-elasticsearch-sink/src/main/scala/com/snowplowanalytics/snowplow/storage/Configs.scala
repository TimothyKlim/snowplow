package com.snowplowanalytics.snowplow.storage

import com.typesafe.config.Config

final case class KafkaConfig(
    host: String,
    topic: String,
    group: String
)

object KafkaConfig {
  def apply(config: Config): KafkaConfig = {
    val kafkaConf = config.getConfig("kafka")
    KafkaConfig(
      host = kafkaConf.getString("host"),
      topic = kafkaConf.getString("topic"),
      group = kafkaConf.getString("group")
    )
  }
}

final case class ElasticConfig(
    endpoint: String,
    cluster: String,
    port: Int,
    documentIndex: String,
    documentType: String
)

object ElasticConfig {
  def apply(config: Config): ElasticConfig = {
    val elasticConf = config.getConfig("elastic")
    ElasticConfig(
      endpoint = elasticConf.getString("endpoint"),
      cluster = elasticConf.getString("cluster"),
      port = elasticConf.getInt("port"),
      documentIndex = elasticConf.getString("document-index"),
      documentType = elasticConf.getString("document-type")
    )
  }
}

final case class PostgresConfig(
    url: String,
    user: String,
    password: String
)

object PostgresConfig {
  def apply(config: Config): PostgresConfig = {
    val postgresConf = config.getConfig("postgres")
    PostgresConfig(
      url = postgresConf.getString("url"),
      user = postgresConf.getString("user"),
      password = postgresConf.getString("password")
    )
  }
}

sealed trait SourceConfig

object SourceConfig {
  def apply(config: Config): SourceConfig =
    config.getString("type") match {
      case "kafka" => KafkaSourceConfig(KafkaConfig(config))
      // case "stdin" => StdinSourceConfig(config)
      case _ => throw new IllegalArgumentException("Unknown or empty `stream.source.type`")
    }
}

final case class KafkaSourceConfig(kafka: KafkaConfig) extends SourceConfig

sealed trait SinkConfig

object SinkConfig {
  def apply(config: Config): SinkConfig =
    config.getString("type") match {
      // case "kafka" => KafkaSinkConfig(config)
      case "elastic"  => ElasticSinkConfig(ElasticConfig(config))
      case "postgres" => PostgresSinkConfig(PostgresConfig(config))
      // case "stdout" => StdoutSinkConfig(config)
      case _ => throw new IllegalArgumentException("Unknown or empty `stream.sink.type`")
    }
}

final case class ElasticSinkConfig(config: ElasticConfig) extends SinkConfig

final case class PostgresSinkConfig(config: PostgresConfig) extends SinkConfig

final case class AppConfig(
    source: SourceConfig,
    sink: SinkConfig
)

object AppConfig {
  def apply(config: Config): AppConfig = {
    val source = SourceConfig(config.getConfig("source"))
    val sink   = SinkConfig(config.getConfig("sink"))
    AppConfig(source, sink)
  }
}
