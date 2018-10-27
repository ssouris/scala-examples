package com.ssouris

import akka.http.caching.LfuCache
import akka.http.caching.scaladsl.{Cache, CachingSettings}

import scala.concurrent.duration._

trait ExternalApiResponseCache {

  implicit val defaultCachingSettings: CachingSettings

  lazy val cachingSettings: CachingSettings =
    defaultCachingSettings.withLfuCacheSettings(defaultCachingSettings.lfuCacheSettings
      .withInitialCapacity(50)
      .withMaxCapacity(100)
      .withTimeToLive(10.seconds) // ttl from creation time
    )

  lazy val externalApiCache: Cache[String, ExternalApiResponse] = LfuCache(cachingSettings)


}
