package system

import com.google.inject.{AbstractModule, Singleton}
import play.api.cache.CacheApi

import scala.concurrent.duration.Duration
import scala.reflect.ClassTag

@Singleton
class NullCacheApi extends CacheApi {

  override def set(key: String, value: Any, expiration: Duration) = ()

  override def get[T: ClassTag](key: String) = None

  override def getOrElse[A: ClassTag](key: String, expiration: Duration)(orElse: => A) = orElse

  override def remove(key: String) = ()

}

class NullCacheModule extends AbstractModule {

  override def configure() = {
    bind(classOf[CacheApi]).to(classOf[NullCacheApi])
  }

}