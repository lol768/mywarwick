package services.reporting

import com.google.inject.ImplementedBy
import javax.inject.{Inject, Named}
import org.joda.time.Interval
import play.api.cache.SyncCacheApi
import services.dao.DepartmentInfoDao
import services.elasticsearch.ClientESService
import warwick.core.Logging
import warwick.sso.{Department, UserLookupService, Usercode}

import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.duration._

@ImplementedBy(classOf[ClientReportingServiceImpl])
trait ClientReportingService {
  def available: Boolean
  def countUniqueUsers(interval: Interval): Int
  def countAppUsers(interval: Interval): Int
  def countWebUsers(interval: Interval): Int
  def countUniqueUsersByDepartment(interval: Interval): Map[Option[Department], Int]
  def countUniqueUsersByType(interval: Interval): Map[String, Int]
}

class ClientReportingServiceImpl @Inject()(
  clientESService: ClientESService,
  departmentsDao: DepartmentInfoDao,
  userLookupService: UserLookupService,
  cache: SyncCacheApi
)(implicit @Named("elastic") ec: ExecutionContext) extends ClientReportingService with Logging {

  private val cacheLifetime = 30.seconds
  private val waitLifetime = 120.seconds
  
  private def cachedAccesses(interval: Interval): Seq[UserAccess] = {
    val cacheKey = s"clientAccesses$interval"

    cache.get[Seq[UserAccess]](cacheKey) match {
      case Some(accesses) => accesses
      case None =>
        val cachingAccesses = clientESService.fetchAccesses(interval).map { accesses =>
          cache.set(cacheKey, accesses, cacheLifetime)
          accesses
        }

        // I don't love this, but my caches
        Await.result(cachingAccesses, waitLifetime)
    }
  }
  
  def available: Boolean = clientESService.available

  def countUniqueUsers(interval: Interval): Int = {
    cachedAccesses(interval).map(_.usercode).distinct.length
  }

  def countAppUsers(interval: Interval): Int = {
    cachedAccesses(interval).distinct.filter(_.userAgent.isApp).length
  }

  def countWebUsers(interval: Interval): Int = {
    cachedAccesses(interval).distinct.filterNot(_.userAgent.isApp).length
  }

  def countUniqueUsersByDepartment(interval: Interval): Map[Option[Department], Int] = {
    val uniqueUsercodes = cachedAccesses(interval).map(_.usercode).distinct
    val allUsers = userLookupService.getUsers(uniqueUsercodes).getOrElse(Map.empty)
    val deptOccurrences = uniqueUsercodes.collect {
      case usercode if allUsers.isDefinedAt(usercode) => allUsers(usercode).department
    }

    deptOccurrences.distinct.map(x => (x, deptOccurrences.count(_ == x))).toMap
  }

  def countUniqueUsersByType(interval: Interval): Map[String, Int] = {
    import utils.UserLookupUtils.UserStringer

    val uniqueUsercodes = cachedAccesses(interval).map(_.usercode).distinct
    val allUsers = userLookupService.getUsers(uniqueUsercodes).getOrElse(Map.empty)
    val typeOccurrences = uniqueUsercodes.collect {
      case usercode if allUsers.isDefinedAt(usercode) => allUsers(usercode).toTypeString
    }

    typeOccurrences.distinct.map(x => (x, typeOccurrences.count(_ == x))).toMap
  }
}


case class UserAccess(usercode: Usercode, userAgent: UserAgent)

object UserAccess {
  def apply(usercode: String, userAgent: UserAgent): UserAccess = {
    UserAccess(Usercode(usercode), userAgent)
  }

  def apply(usercode: String, device: String, os: String, ua: String): UserAccess = {
    UserAccess(Usercode(usercode), UserAgent(device: String, os: String, ua: String))
  }
}

case class UserAgent(device: String, os: String, appVersion: Option[String]) {
  def isApp: Boolean = appVersion.isDefined
}

object UserAgent {
  def apply(device: String, os: String, ua: String): UserAgent = {
    val appVersion = ua.split(" ").last match {
      case s if s.matches("MyWarwick/\\d+") => Some(s.split("/").last)
      case _ => None
    }
    UserAgent(device, os, appVersion)
  }
}

