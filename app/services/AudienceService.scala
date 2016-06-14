package services

import javax.inject.Inject

import com.google.inject.ImplementedBy
import models.news.Audience
import models.news.Audience._
import system.Logging
import warwick.sso.{GroupName, GroupService, Usercode}

import scala.util.Try



@ImplementedBy(classOf[AudienceServiceImpl])
trait AudienceService {
  def resolve(audience: Audience): Try[Seq[Usercode]]
}

class AudienceServiceImpl @Inject()(
  webgroups: GroupService
) extends AudienceService with Logging {

  // TODO Try.get wrapped with another Try is a weak sauce solution.
  // Should use magic combinators that nobody can understand.
  override def resolve(audience: Audience): Try[Seq[Usercode]] = Try {
    if (audience.public) {
      Seq(Usercode("*"))
    } else {
      audience.components.flatMap {
        // webgroups has handy "all-" webgroups that subset all the departments.
        case ds: DepartmentSubset => resolveSubset("all", ds).get
        case WebgroupAudience(name) => webgroupUsers(name).get
        case ModuleAudience(code) => moduleWebgroupUsers(code).get
        case DepartmentAudience(code, subsets) => for {
          subset <- subsets
          user <- resolveSubset(code.toLowerCase, subset).get
        } yield user
      }.distinct
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
    webgroups.getWebGroup(groupName).map { group =>
      group.map(_.members).getOrElse(Nil)
    }

  def moduleWebgroupUsers(code: String): Try[Seq[Usercode]] =
    webgroups.getGroupsForQuery(s"-${code.toLowerCase}").map { groups =>
      groups.find(group => group.name.string.endsWith(code.toLowerCase) && group.`type` == "Module")
        .map(_.members)
        .getOrElse(Nil)
    }


}

