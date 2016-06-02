package services

import play.api.cache.CacheApi

import scala.concurrent.duration.Duration
import scala.reflect.ClassTag

class MockCacheApi extends CacheApi {
  def set(key: String, value: Any, expiration: Duration) = ()

  def get[T: ClassTag](key: String) = None

  def getOrElse[A: ClassTag](key: String, expiration: Duration)(orElse: => A) = orElse

  def remove(key: String) = ()
}
