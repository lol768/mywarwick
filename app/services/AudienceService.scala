package services

import javax.inject.Inject

import com.google.inject.ImplementedBy
import models.Audience
import models.Audience._
import play.api.db.Database
import services.dao.{AudienceDao, UserNewsOptInDao}
import system.Logging
import warwick.sso.{GroupName, GroupService, Usercode}

import scala.util.Try

@ImplementedBy(classOf[AudienceServiceImpl])
trait AudienceService {
  def resolve(audience: Audience): Try[Set[Usercode]]
  def getAudience(audienceId: String): Audience
}

class AudienceServiceImpl @Inject()(
  groupService: GroupService,
  dao: AudienceDao,
  optInDao: UserNewsOptInDao,
  db: Database
) extends AudienceService with Logging {

  // TODO Try.get wrapped with another Try is a weak sauce solution.
  // Should use magic combinators that nobody can understand.
  override def resolve(audience: Audience): Try[Set[Usercode]] = Try {
    val (optInComponents, audienceComponents) = audience.components.partition {
      case _: OptIn => true
      case _ => false
    }
    val audienceUsers = audienceComponents.flatMap {
      case PublicAudience => Seq(Usercode("*"))
      // webgroups has handy "all-" webgroups that subset all the departments.
      case ds: DepartmentSubset => resolveSubset("all", ds).get
      case WebGroupAudience(name) => webgroupUsers(name).get
      case ModuleAudience(code) => moduleWebgroupUsers(code).get
      case DepartmentAudience(code, subsets) => for {
        subset <- subsets
        user <- resolveSubset(code.toLowerCase, subset).get
      } yield user
      case UsercodeAudience(usercode) => Seq(usercode)
      case optIn: OptIn => db.withConnection(implicit c => optInDao.getUsercodes(optIn))
    }.toSet

    if (optInComponents.nonEmpty) {
      // AND each opt-in type with the selected audience

      val optInUsersByType = optInComponents.collect { case o: OptIn => o }
        .groupBy(_.optInType)
        .values.toSeq.map(_.flatMap(o => db.withConnection(implicit c => optInDao.getUsercodes(o))).toSet)

      val optInUsers = optInUsersByType.tail.foldLeft(optInUsersByType.head) { case (result, usercodes) => result.intersect(usercodes) }

      audienceUsers.intersect(optInUsers)
    } else {
      audienceUsers
    }
  }

  private def resolveSubset(deptCode: String, component: DepartmentSubset): Try[Seq[Usercode]] =
    component match {
      case All => webgroupUsers(GroupName(s"$deptCode-all"))
      case Staff => webgroupUsers(GroupName(s"$deptCode-staff"))
      case UndergradStudents => for {
        ft <- webgroupUsers(GroupName(s"$deptCode-studenttype-undergraduate-full-time"))
        pt <- webgroupUsers(GroupName(s"$deptCode-studenttype-undergraduate-part-time"))
      } yield ft ++ pt
      case ResearchPostgrads => for {
        ft <- webgroupUsers(GroupName(s"$deptCode-studenttype-postgraduate-research-ft"))
        pt <- webgroupUsers(GroupName(s"$deptCode-studenttype-postgraduate-research-pt"))
      } yield ft ++ pt
      case TaughtPostgrads => for {
        ft <- webgroupUsers(GroupName(s"$deptCode-studenttype-postgraduate-taught-ft"))
        pt <- webgroupUsers(GroupName(s"$deptCode-studenttype-postgraduate-taught-pt"))
      } yield ft ++ pt
      case TeachingStaff => webgroupUsers(GroupName(s"$deptCode-teaching"))
    }

  private def webgroupUsers(groupName: GroupName): Try[Seq[Usercode]] =
    groupService.getWebGroup(groupName).map { group =>
      group.map(_.members).getOrElse(Nil)
    }

  def moduleWebgroupUsers(code: String): Try[Seq[Usercode]] =
    groupService.getGroupsForQuery(s"-${code.toLowerCase}").map { groups =>
      groups.find(group => group.name.string.endsWith(code.toLowerCase) && group.`type` == "Module")
        .map(_.members)
        .getOrElse(Nil)
    }

  override def getAudience(audienceId: String): Audience =
    db.withConnection(implicit c => dao.getAudience(audienceId))
}
