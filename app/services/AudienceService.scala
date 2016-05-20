package services

import javax.inject.Inject

import models.news.Audience
import models.news.Audience._
import system.Logging
import warwick.sso.{GroupName, GroupService, Usercode}

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
      case _ => ???
    })

  private def resolveSubset(deptCode: String, component: DepartmentSubset): Try[Seq[Usercode]] =
    component match {
      case Staff => webgroupUsers(GroupName(s"$deptCode-staff"))
      case UndergradStudents => for {
        ft <- webgroupUsers(GroupName(s"$deptCode-undergraduate-full-time"))
        pt <- webgroupUsers(GroupName(s"$deptCode-undergraduate-part-time"))
      } yield ft ++ pt
      case ResearchPostgrads => for {
          ft <- webgroupUsers(GroupName(s"$deptCode-postgraduate-research-ft"))
          pt <- webgroupUsers(GroupName(s"$deptCode-postgraduate-research-pt"))
        } yield ft ++ pt
      case TaughtPostgrads => throw new MatchError("Not done this yet, you dingus!")
      case _ => ???
    }

  private def webgroupUsers(groupName: GroupName): Try[Seq[Usercode]] =
    webgroups.getWebGroup(groupName).map { group =>
      group.map(_.members).getOrElse(Nil)
    }


}

