package com.snowplowanalytics.snowplow.storage.postgres

import com.github.tminglei.slickpg._

trait Profile extends ExPostgresProfile {
  // override val api = new API {}
}

object Profile extends Profile
