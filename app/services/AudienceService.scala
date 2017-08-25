package services

import javax.inject.{Inject, Named}

import com.google.inject.ImplementedBy
import models.Audience
import models.Audience._
import play.api.db.Database
import services.dao.{AudienceDao, AudienceLookupDao, UserNewsOptInDao}
import system.Logging
import warwick.sso.{GroupName, GroupService, Usercode}

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.util.Try

@ImplementedBy(classOf[AudienceServiceImpl])
trait AudienceService {
  def resolve(audience: Audience): Try[Seq[Usercode]]
  def getAudience(audienceId: String): Audience
}

class AudienceServiceImpl @Inject()(
  groupService: GroupService,
  dao: AudienceDao,
  optInDao: UserNewsOptInDao,
  @Named("tabula") audienceLookupDao: AudienceLookupDao,
  db: Database
) extends AudienceService with Logging {

  import system.ThreadPools.externalData

  override def resolve(audience: Audience): Try[Seq[Usercode]] = {
    Await.ready(resolveFuture(audience), 30.seconds).value.get
  }

  private def resolveFuture(audience: Audience): Future[Seq[Usercode]] = {
    val (optInComponents, audienceComponents) = audience.components.partition {
      case _: OptIn => true
      case _ => false
    }
    val audienceUsers: Future[Set[Usercode]] = Future.sequence(audienceComponents.map {
      case PublicAudience => Future.successful(Seq(Usercode("*")))
      // A subset not in a department i.e. ALL undergraduates in the University
      // Use WebGroups for these
      case ds: DepartmentSubset => Future.fromTry(resolveUniversityGroup(ds))
      case WebGroupAudience(name) => Future.fromTry(webgroupUsers(name))
      case DepartmentAudience(code, subsets) => Future.sequence(subsets.map(subset =>
        resolveDepartmentGroup(code, subset)
      )).map(_.flatten.toSeq)
      case ModuleAudience(code) => audienceLookupDao.resolveModule(code)
      case SeminarGroupAudience(groupId) => audienceLookupDao.resolveSeminarGroup(groupId)
      case RelationshipAudience(relationshipType, agentId) => audienceLookupDao.resolveRelationship(agentId, relationshipType)
      case UsercodeAudience(usercode) => Future.successful(Seq(usercode))
      case UsercodesAudience(usercodes) => Future.successful(usercodes)
      case optIn: OptIn => Future.successful(Nil) // Handled below
    }).map(_.flatten.toSet)

    if (optInComponents.nonEmpty) {
      // AND each opt-in type with the selected audience

      val optInUsersByType = optInComponents.collect { case o: OptIn => o }
        .groupBy(_.optInType)
        .values.toSeq.map(_.flatMap(o => db.withConnection(implicit c => optInDao.getUsercodes(o))).toSet)

      val optInUsers = optInUsersByType.tail.foldLeft(optInUsersByType.head) { case (result, usercodes) => result.intersect(usercodes) }

      audienceUsers.map(_.intersect(optInUsers).toSeq)
    } else {
      audienceUsers.map(_.toSeq)
    }
  }

  private def resolveUniversityGroup(component: DepartmentSubset): Try[Seq[Usercode]] =
    component match {
      case All => webgroupUsers(GroupName("all-all"))
      case Staff => webgroupUsers(GroupName("all-staff"))
      case UndergradStudents => for {
        ft <- webgroupUsers(GroupName("all-studenttype-undergraduate-full-time"))
        pt <- webgroupUsers(GroupName("all-studenttype-undergraduate-part-time"))
      } yield ft ++ pt
      case ResearchPostgrads => for {
        ft <- webgroupUsers(GroupName("all-studenttype-postgraduate-research-ft"))
        pt <- webgroupUsers(GroupName("all-studenttype-postgraduate-research-pt"))
      } yield ft ++ pt
      case TaughtPostgrads => for {
        ft <- webgroupUsers(GroupName("all-studenttype-postgraduate-taught-ft"))
        pt <- webgroupUsers(GroupName("all-studenttype-postgraduate-taught-pt"))
      } yield ft ++ pt
      case TeachingStaff => webgroupUsers(GroupName(s"all-teaching"))
      case AdminStaff =>
        // Webgroups has no concept of 'admin staff' so assume it's all staff that aren't teaching staff
        webgroupUsers(GroupName("all-staff")).flatMap(allStaff =>
          webgroupUsers(GroupName(s"all-teaching")).map(teachingStaff => allStaff.diff(teachingStaff))
        )
    }


  private def resolveDepartmentGroup(departmentCode: String, subset: DepartmentSubset): Future[Seq[Usercode]] =
    subset match {
      case All => audienceLookupDao.resolveDepartment(departmentCode)
      case Staff => Future.sequence(Seq(
        audienceLookupDao.resolveAdminStaff(departmentCode),
        audienceLookupDao.resolveTeachingStaff(departmentCode)
      )).map(_.flatten.toSeq)
      case UndergradStudents => audienceLookupDao.resolveUndergraduates(departmentCode)
      case ResearchPostgrads => audienceLookupDao.resolveResearchPostgraduates(departmentCode)
      case TaughtPostgrads => audienceLookupDao.resolveTaughtPostgraduates(departmentCode)
      case TeachingStaff => audienceLookupDao.resolveTeachingStaff(departmentCode)
      case AdminStaff => audienceLookupDao.resolveAdminStaff(departmentCode)
    }

  private def webgroupUsers(groupName: GroupName): Try[Seq[Usercode]] =
    groupService.getWebGroup(groupName).map { group =>
      group.map(_.members).getOrElse(Nil)
    }

  override def getAudience(audienceId: String): Audience =
    db.withConnection(implicit c => dao.getAudience(audienceId))
}
