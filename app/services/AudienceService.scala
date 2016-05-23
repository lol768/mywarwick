package services

import javax.inject.Inject

import models.news.Audience
import models.news.Audience._
import system.Logging
import warwick.sso.{Group, GroupName, GroupService, Usercode}

import scala.util.Try

trait AudienceService {
  def resolve(audience: Audience): Try[Seq[Usercode]]
}

class AudienceServiceImpl @Inject() (webgroups: GroupService) extends AudienceService with Logging {

  override def resolve(audience: Audience): Try[Seq[Usercode]] =
    // TODO Try.get wrapped with another Try is a weak sauce solution.
    // Should use magic folds that nobody can understand.
    Try(audience.components.flatMap {
      // webgroups has handy "all-" webgroups that subset all the departments.
      case ds: DepartmentSubset => resolveSubset("all", ds).get
      case WebgroupAudience(name) => webgroupUsers(name).get
      case ModuleAudience(code) => moduleWebgroupUsers(code).get
      case DepartmentAudience(code, subsets) => subsets.flatMap{ subset =>
        resolveSubset(code, subset).get
      }
    })

  private def resolveSubset(deptCode: String, component: DepartmentSubset): Try[Seq[Usercode]] =
    component match {
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
    webgroups.getGroupsForQuery(s"-${code}").map { groups =>
      groups.find(group => group.name.string.endsWith(code) && group.`type` == "Module")
        .map(_.members)
        .getOrElse(Nil)
    }


}

