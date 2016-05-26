package services.dao

import javax.inject.Inject

import com.google.inject.ImplementedBy
import play.api.Configuration
import play.api.cache.CacheApi
import play.api.libs.json._
import play.api.libs.ws.{WSAPI, WSRequest}
import system.{CacheMethods, Logging}

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

case class DepartmentInfo(
  code: String,
  name: String,
  shortName: String,
  veryShortName: String,
  `type`: String
)

object DepartmentInfo {
  implicit val jsReads = Json.reads[DepartmentInfo]
}

@ImplementedBy(classOf[WsDepartmentInfoDao])
trait DepartmentInfoDao {
  def allDepartments: Future[Seq[DepartmentInfo]]
}

class WsDepartmentInfoDao @Inject() (
  cache: CacheApi,
  ws: WSAPI,
  config: Configuration
) extends DepartmentInfoDao with Logging {
  import CacheMethods._
  import system.ThreadPools.externalData

  private lazy val url: String = config.getString("departments.list.url")
    .getOrElse(throw new IllegalArgumentException("departments.list.url missing"))

  private lazy val request: WSRequest = ws.url(url).withRequestTimeout(5000)

  override def allDepartments: Future[Seq[DepartmentInfo]] = cache.getOrElseFuture("allDepartmentInfo", 1.hour) {
    request.get.map { response =>
      response.json.as[Seq[DepartmentInfo]]
    }.recover { case e =>
      logger.error("Error fetching departments", e)
      throw e
    }
  }

}



