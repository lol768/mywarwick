package services

import javax.inject.{Inject, Named}

import com.google.inject.ImplementedBy
import models.Audience
import models.Audience.{LocationOptIn, _}
import play.api.db.Database
import play.api.libs.json.{JsValue, Json}
import services.dao._
import system.Logging
import warwick.sso._

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success, Try}

@ImplementedBy(classOf[AudienceServiceImpl])
trait AudienceService {
  def resolve(audience: Audience): Try[Set[Usercode]]

  def getAudience(audienceId: String): Audience

  def resolveUsersForComponentsGrouped(audienceComponents: Seq[Audience.Component]): Try[Seq[(Audience.Component, Set[Usercode])]]

  def audienceToJson(audience: Audience): JsValue

  def validateUsercodes(usercodes: Set[Usercode]): Either[Set[Usercode], Set[Usercode]]
}

class AudienceServiceImpl @Inject()(
  groupService: GroupService,
  dao: AudienceDao,
  optInDao: UserNewsOptInDao,
  @Named("tabula") audienceLookupDao: AudienceLookupDao,
  db: Database,
  userLookupService: UserLookupService
) extends AudienceService with Logging {

  import system.ThreadPools.externalData

  override def resolve(audience: Audience): Try[Set[Usercode]] = {
    Await.ready(resolveFuture(audience), 30.seconds).value.get
  }

  private def resolveUsersForComponent(audienceComponent: Audience.Component): Future[Set[Usercode]] = resolveUsersForComponentWithGroup(audienceComponent).map(
    _.flatMap {
      case (_, usercodes) => usercodes
    }
  ).map(_.toSet)


  private def resolveUsersForComponentWithGroup(audienceComponent: Audience.Component): Future[Seq[(Audience.Component, Set[Usercode])]] = {
    def makeResult(futureUsercodes: Future[Iterable[Usercode]], group: Audience.Component = audienceComponent): Future[Seq[(Audience.Component, Set[Usercode])]] = {
      futureUsercodes.map { usercodes =>
        Seq(
          (group, usercodes.toSet),
        )
      }
    }

    audienceComponent match {
      case PublicAudience => makeResult(Future.successful(Seq(Usercode("*"))))
      case WebGroupAudience(name) => makeResult(Future.fromTry(webgroupUsers(name)))
      case ModuleAudience(code) => makeResult(audienceLookupDao.resolveModule(code))
      case SeminarGroupAudience(groupId) => makeResult(audienceLookupDao.resolveSeminarGroup(groupId))
      case RelationshipAudience(relationshipType, agentId) => makeResult(audienceLookupDao.resolveRelationship(agentId, relationshipType))
      case UsercodesAudience(usercodes) => makeResult(Future.successful(usercodes))
      case ds: DepartmentSubset => makeResult(Future.fromTry(resolveUniversityGroup(ds)))
      case DepartmentAudience(code, subsets) => Future.sequence(subsets.map(subset =>
        makeResult(resolveDepartmentGroup(code, subset), subset)
      )).map(_.flatten)
      case optIn: OptIn => makeResult(Future.successful(Nil))
    }
  }

  def resolveUsersForComponents(audienceComponents: Seq[Audience.Component]): Future[Set[Usercode]] = {
    Future.sequence(audienceComponents.map(this.resolveUsersForComponent)).map(_.flatten.toSet)
  }

  override def resolveUsersForComponentsGrouped(audienceComponents: Seq[Audience.Component]): Try[Seq[(Audience.Component, Set[Usercode])]] = {
    Await.ready(Future.sequence(audienceComponents.map(this.resolveUsersForComponentWithGroup)).map(_.flatten), 30.seconds).value.get
  }

  def resolveFuture(audience: Audience): Future[Set[Usercode]] = {
    val (optInComponents, audienceComponents) = audience.components.partition {
      case _: OptIn => true
      case _ => false
    }

    val audienceUsers = this.resolveUsersForComponents(audienceComponents)

    if (optInComponents.nonEmpty) {
      // AND each opt-in type with the selected audience

      val optInUsersByType = optInComponents.collect { case o: OptIn => o }
        .groupBy(_.optInType)
        .values.toSeq.map(_.flatMap(o => db.withConnection(implicit c => optInDao.getUsercodes(o))).toSet)

      val optInUsers = optInUsersByType.tail.foldLeft(optInUsersByType.head) { case (result, usercodes) => result.intersect(usercodes) }

      audienceUsers.map(_.intersect(optInUsers))
    } else {
      audienceUsers
    }
  }

  private def resolveUniversityGroup(component: DepartmentSubset): Try[Seq[Usercode]] =
    component match {
      case All => webgroupUsers(GroupName("all-all"))
      case Staff => webgroupUsers(GroupName("all-staff"))
      case ug: UndergradStudents => ug match {
        case UndergradStudents.All => for {
          ft <- webgroupUsers(GroupName("all-studenttype-undergraduate-full-time"))
          pt <- webgroupUsers(GroupName("all-studenttype-undergraduate-part-time"))
        } yield ft ++ pt
        case UndergradStudents.First => webgroupUsers(GroupName("all-level-1"))
        case UndergradStudents.Second => webgroupUsers(GroupName("all-level-2"))
        case UndergradStudents.Final => ???
        case _ => Success(Nil)
      }
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
      case _ => Try(Nil)
    }


  private def resolveDepartmentGroup(departmentCode: String, subset: DepartmentSubset): Future[Seq[Usercode]] =
    subset match {
      case All => audienceLookupDao.resolveDepartment(departmentCode)
      case Staff => Future.sequence(Seq(
        audienceLookupDao.resolveAdminStaff(departmentCode),
        audienceLookupDao.resolveTeachingStaff(departmentCode)
      )).map(_.flatten.toSeq)
      case ug: UndergradStudents => {
        import UndergradStudents._
        ug match {
          case All => audienceLookupDao.resolveUndergraduates(departmentCode, All)
          case First => audienceLookupDao.resolveUndergraduates(departmentCode, First)
          case Second => audienceLookupDao.resolveUndergraduates(departmentCode, Second)
          case Final => audienceLookupDao.resolveUndergraduates(departmentCode, Final)
        }
      }
      case ResearchPostgrads => audienceLookupDao.resolveResearchPostgraduates(departmentCode)
      case TaughtPostgrads => audienceLookupDao.resolveTaughtPostgraduates(departmentCode)
      case TeachingStaff => audienceLookupDao.resolveTeachingStaff(departmentCode)
      case AdminStaff => audienceLookupDao.resolveAdminStaff(departmentCode)
      case ModuleAudience(code) => audienceLookupDao.resolveModule(code)
      case SeminarGroupAudience(groupId) => audienceLookupDao.resolveSeminarGroup(groupId)
      case RelationshipAudience(relationshipType, agentId) => audienceLookupDao.resolveRelationship(agentId, relationshipType)
      case UsercodesAudience(usercodes) => Future.successful(usercodes.toSeq)
    }

  private def webgroupUsers(groupName: GroupName): Try[Seq[Usercode]] =
    groupService.getWebGroup(groupName).map { group =>
      group.map(_.members).getOrElse(Nil)
    }

  override def getAudience(audienceId: String): Audience =
    db.withConnection(implicit c => dao.getAudience(audienceId))


  override def audienceToJson(audience: Audience): JsValue = {

    def resolveStaffRelationship(agentId: UniversityID, checkedRelationships: Seq[String]): Future[JsValue] = {
      audienceLookupDao.findRelationships(agentId).map { rel =>
        Json.obj(
          "value" -> agentId.string,
          "text" -> userLookupService.getUsers(Seq(agentId)).get.get(agentId).map {
            case u: User =>
              s"${u.name.full.getOrElse("")} ${if (u.department.isDefined) s"(${u.department.get.name.get})"}"
            case _ => ""
          },
          "options" -> rel.map {
            case (r: LookupRelationshipType, users: Seq[User]) => Json.obj(
              r.id -> Json.obj(
                "agentRole" -> r.agentRole,
                "studentRole" -> r.studentRole,
                "students" -> users.map(_.name.full),
                "selected" -> checkedRelationships.contains(r.id)
              )
            )
          }
        )
      }
    }

    var department: String = ""
    var departmentSubsets: Seq[String] = Seq.empty[String]
    var undergradSubsets: Seq[String] = Seq.empty[String]
    var listOfUsercodes: Seq[String] = Seq.empty[String]
    var modules: Seq[JsValue] = Seq.empty[JsValue]
    var seminarGroups: Seq[JsValue] = Seq.empty[JsValue]
    var locations: Seq[String] = Seq.empty[String]
    var staffRelationships: Map[UniversityID, Seq[String]] = Map[UniversityID, Seq[String]]()

    def matchDeptSubset(subset: DepartmentSubset): Unit =
      subset match {
        case ModuleAudience(code) =>
          modules ++= Await.result(audienceLookupDao.findModules(code.trim), 5.seconds).map { m =>
            Json.obj(
              "value" -> m.code.toUpperCase,
              "text" -> s"${m.code.toUpperCase}: ${m.name}"
            )
          }
        case SeminarGroupAudience(groupId) =>
          seminarGroups ++= Await.result(audienceLookupDao.getSeminarGroupById(groupId.trim), 5.seconds).map { group =>
            Json.obj(
              "value" -> groupId,
              "text" -> s"${group.name}" //: ${group.groupSetName}"
            )
          }
        case RelationshipAudience(relationshipType, agentId) =>
          staffRelationships += agentId -> (staffRelationships.getOrElse(agentId, Seq.empty[String]) :+ relationshipType)
        case UsercodesAudience(usercodes) => listOfUsercodes ++= usercodes.map(_.string)
        case _ => Nil
      }

    audience.components.foreach {
      case ds: DepartmentSubset => ds match {
        case UndergradStudents.All | UndergradStudents.First | UndergradStudents.Second | UndergradStudents.Final  =>
          undergradSubsets :+= s"UndergradStudents:${ds.toString}"
        case All | TeachingStaff | ResearchPostgrads | TaughtPostgrads | AdminStaff =>
          departmentSubsets :+= ds.toString
        case subset => matchDeptSubset(subset)
      }
      case DepartmentAudience(code, subsets) => {
        department = code
        subsets.foreach {
          case subset@(UndergradStudents.All | UndergradStudents.First | UndergradStudents.Second | UndergradStudents.Final)  =>
            undergradSubsets :+= s"Dept:UndergradStudents:${subset.toString}"
          case subset@(All | TeachingStaff | ResearchPostgrads | TaughtPostgrads | AdminStaff) =>
            departmentSubsets :+= s"Dept:${subset.entryName}"
          case subset => matchDeptSubset(subset)
        }
      }
      case optIn: OptIn if optIn.optInType == LocationOptIn.optInType => locations :+= optIn.optInValue
      case _ => Nil
    }

    val audienceType =
      if (department.isEmpty)
        "universityWide"
      else "department"

    val locationsJson =
      if (locations.nonEmpty) Json.obj("locations" -> Json.obj("yesLocation" ->
        Json.obj(locations.map(l => l -> Json.toJsFieldJsValueWrapper("undefined")): _*)))
      else Json.obj()

    val staffRelationshipJson =
      if (staffRelationships.nonEmpty)
        Json.obj("staffRelationships" ->
          staffRelationships.map { case (k, v) =>
            Await.result(resolveStaffRelationship(k, v), 5.second)
          }.toSeq
        )
      else Json.obj()

    val seminarGroupsJson =
      if (seminarGroups.nonEmpty)
        Json.obj("seminarGroups" -> seminarGroups)
      else Json.obj()

    val modulesJson =
      if (modules.nonEmpty)
        Json.obj("modules" -> modules)
      else Json.obj()

    val listOfUsercodesJson =
      if (listOfUsercodes.nonEmpty)
        Json.obj("listOfUsercodes" -> listOfUsercodes)
      else Json.obj()

    println(undergradSubsets)

    val undergraduates =
      if (undergradSubsets.nonEmpty)
        if (undergradSubsets.contains("all"))
          Json.obj("undergraduates" -> s"${if (department.isEmpty) "" else "Dept:"}UndergradStudents:All")
        else
          Json.obj("undergraduates" -> Json.obj("year" -> Json.obj(
            undergradSubsets.map(_ -> Json.toJsFieldJsValueWrapper("undefined")): _*
          )))
      else Json.obj()

    val deptSubsets: (String, Json.JsValueWrapper) =
      if (departmentSubsets.contains("Dept:All"))
        "Dept:All" -> Json.toJsFieldJsValueWrapper("undefined")
      else
        "groups" -> (Json.obj(
          departmentSubsets.map(_ -> Json.toJsFieldJsValueWrapper("undefined")): _*
        ) ++ staffRelationshipJson ++ seminarGroupsJson ++ modulesJson ++ listOfUsercodesJson ++ undergraduates)

    Json.obj(
      "department" -> department,
      "audience" -> Json.obj(
        audienceType -> Json.obj(
          deptSubsets
        )
      )
    ) ++ locationsJson
  }

  override def validateUsercodes(usercodes: Set[Usercode]): Either[Set[Usercode], Set[Usercode]] = {
    val uniIds: Set[String] = usercodes.map(_.string).filter(_.forall(Character.isDigit))
    val validIds: Option[Map[UniversityID, User]] = userLookupService.getUsers(uniIds.map(id => UniversityID(id)).toSeq).toOption
    val validCodes: Set[Usercode] = userLookupService.getUsers(usercodes.toSeq).toOption
      .map(_.keys).getOrElse(Nil).toSet

    val invalid: Set[Usercode] = usercodes.diff(validCodes).filterNot(u => validIds.map(_.keys).getOrElse(Nil).map(_.string).toSet.contains(u.string))

    if (invalid.isEmpty) {
      Right(validCodes ++ validIds.map(_.values.map(_.usercode)).getOrElse(Nil).toSet)
    } else {
      Left(invalid)
    }
  }

}
