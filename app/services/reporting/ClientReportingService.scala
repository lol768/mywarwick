package services.reporting

import com.google.inject.ImplementedBy
import javax.inject.{Inject, Named, Singleton}
import org.elasticsearch.search.SearchHit
import org.joda.time.{DateTime, Duration, Interval}
import play.api.cache.SyncCacheApi
import services.dao.DepartmentInfoDao
import services.elasticsearch.ClientESService
import services.reporting.TimedClientMetrics._
import utils.UserLookupUtils.DepartmentStringer
import warwick.core.Logging
import warwick.sso.{Department, UserLookupService, Usercode}

import scala.collection.JavaConverters._
import scala.collection.immutable.ListMap
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

@ImplementedBy(classOf[ClientReportingServiceImpl])
trait ClientReportingService {
  def getKey(interval: Interval): String
  def getInterval(start: String, end: String): Option[Interval]
  def getMetrics(interval: Interval): TimedClientMetrics
  def getCacheLifetime: String
}

@Singleton
class ClientReportingServiceImpl @Inject()(
  clientESService: ClientESService,
  departmentsDao: DepartmentInfoDao,
  userLookupService: UserLookupService,
  cache: SyncCacheApi
)(implicit @Named("elastic") ec: ExecutionContext) extends ClientReportingService with Logging {

  private val cacheLifetime = 6.hours
    
  private def countUniqueUsers(accesses: Seq[UserAccess]): Int = {
    accesses.map(_.usercode).distinct.length
  }

  private def countAppUsers(accesses: Seq[UserAccess]): Int = {
    accesses.distinct.count(_.userAgent.isApp)
  }

  private def countWebUsers(accesses: Seq[UserAccess]): Int = {
    accesses.distinct.filterNot(_.userAgent.isApp).length
  }

  private def countUniqueUsersByDepartment(accesses: Seq[UserAccess]): ListMap[Option[Department], Int] = {
    val uniqueUsercodes = accesses.map(_.usercode).distinct
    val allUsers = userLookupService.getUsers(uniqueUsercodes).getOrElse(Map.empty)
    val deptOccurrences = uniqueUsercodes.collect {
      case usercode if allUsers.isDefinedAt(usercode) => allUsers(usercode).department
    }

    ListMap(deptOccurrences.distinct.map(x => (x, deptOccurrences.count(_ == x))).sortWith(_._2 > _._2):_*)
  }

  private def countUniqueUsersByType(accesses: Seq[UserAccess]): ListMap[String, Int] = {
    import utils.UserLookupUtils.UserStringer

    val uniqueUsercodes = accesses.map(_.usercode).distinct
    val allUsers = userLookupService.getUsers(uniqueUsercodes).getOrElse(Map.empty)
    val typeOccurrences = uniqueUsercodes.collect {
      case usercode if allUsers.isDefinedAt(usercode) => allUsers(usercode).toTypeString
    }

    ListMap(typeOccurrences.distinct.map(x => (x, typeOccurrences.count(_ == x))).sortWith(_._2 > _._2):_*)
  }
  
  override def getKey(interval: Interval): String = {
    s"clientMetrics-${interval.getStartMillis}-${interval.getEndMillis}"
  }

  override def getInterval(startStr: String, endStr: String): Option[Interval] = {
    if (startStr.matches("^\\d+$") && endStr.matches("^\\d+$")) {
        val start = Math.max(0, startStr.toLong)
        val end = Math.min(DateTime.now.getMillis, endStr.toLong)
        if (start > end) {
          Option(new Interval(end, start))
        } else {
          Option(new Interval(start, end))
        }
    } else {
      None
    }
  }

  override def getMetrics(interval: Interval): TimedClientMetrics = {
    var duration: Duration = new Duration(0)
    try {
      val key = getKey(interval)
      cache.get[TimedClientMetrics](key) match {
        case Some(m: TimedClientMetrics) => m
        case None =>
          // push placeholder to cache for subsequent requests
          cache.set(key, TimedClientMetrics(), cacheLifetime)
          
          val cad = clientESService.fetchAccesses(interval)
          duration = cad.duration
          val metrics = TimedClientMetrics(Complete, duration, ClientMetrics(
            countUniqueUsers(cad.accesses),
            countAppUsers(cad.accesses),
            countWebUsers(cad.accesses),
            countUniqueUsersByDepartment(cad.accesses).map({ case (dept, count) => (dept.toSafeString, count) }),
            countUniqueUsersByType(cad.accesses)
          ))
          cache.set(key, metrics, cacheLifetime)
          metrics
      }
    } catch {
      case t: Throwable =>
        logger.error("Problem getting metrics", t)
        TimedClientMetrics(Fault)
    }
  }

  override def getCacheLifetime: String = cacheLifetime.toString
}

case class UserAccess(usercode: Usercode, userAgent: UserAgent)

object UserAccess {
  def apply(usercode: String, userAgent: UserAgent): UserAccess = {
    UserAccess(Usercode(usercode), userAgent)
  }

  def apply(usercode: String, device: String, os: String, ua: String): UserAccess = {
    UserAccess(Usercode(usercode), UserAgent(device: String, os: String, ua: String))
  }

  def fromESSearchHit(sh: SearchHit): UserAccess = {
    fromMap(sh.getSourceAsMap.asScala.toMap)
  }

  private def mapString(hitMap: Map[String, AnyRef], field: String): Option[String] = hitMap.get(field) match {
    case Some(s: String) => Some(s)
    case _ => None
  }

  private def mapSubMap(hitMap: Map[String, AnyRef], field: String): Map[String, AnyRef] = hitMap.get(field) match {
    case Some(m: java.util.Map[String, AnyRef] @unchecked) => m.asScala.toMap
    case _ => Map.empty
  }

  def fromMap(hitMap: Map[String, AnyRef]): UserAccess = {
    val nullSafeMap = hitMap.filterNot { case (_, v) => v == null }
    
    val usercode = mapString(nullSafeMap, "username").getOrElse("-")
    val uaDetail = mapSubMap(nullSafeMap, "user-agent-detail")
    val device = mapString(uaDetail, "device").getOrElse("-")
    val os = mapString(uaDetail, "os").getOrElse("-")
    val headers = mapSubMap(nullSafeMap, "request_headers")
    val ua = mapString(headers, "user-agent").getOrElse("-")
    
    UserAccess(usercode, device, os, ua)
  }
}

case class UserAgent(device: String, os: String, appVersion: Option[String]) {
  def isApp: Boolean = appVersion.isDefined
  override def toString: String = s"UserAgent(v${appVersion.getOrElse("?")} on $device running $os)"
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

case class ClientMetrics(
  uniqueUserCount: Int = 0,
  appUserCount: Int = 0,
  webUserCount: Int =0 ,
  deptUserCount: ListMap[String, Int] = ListMap.empty,
  typedUserCount: ListMap[String, Int] = ListMap.empty
)

case class TimedClientMetrics(state: State = Pending, duration: Duration = new Duration(0), metrics: ClientMetrics = ClientMetrics()) {
  def withDuration(duration: Duration): TimedClientMetrics = this.copy(duration = duration)
  def withDuration(duration: Long): TimedClientMetrics = this.copy(duration = new Duration(duration))
  def isComplete: Boolean = state == Complete
  def isPending: Boolean = state == Pending
  def isFault: Boolean = state == Fault
}

object TimedClientMetrics {
  sealed trait State
  case object Complete extends State
  case object Pending extends State
  case object Fault extends State
}
