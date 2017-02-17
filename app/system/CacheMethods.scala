package system

import play.api.cache.CacheApi

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.Duration
import scala.reflect.ClassTag
import scala.util.Success


object CacheMethods {

  implicit class FutureCache(val c: CacheApi) extends AnyVal {
    /**
      * Like getOrElse, but the value is provided by a Future and so the
      * return type correspondingly is a Future.
      */
    def getOrElseFuture[A: ClassTag](key: String, duration: Duration)(orElse: => Future[A])(implicit ec: ExecutionContext): Future[A] =
      c.get[A](key).map(Future.successful).getOrElse {
        orElse.andThen {
          case Success(result) => c.set(key, result, duration)
        }
      }
  }

}
