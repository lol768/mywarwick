package services

import helpers.{BaseSpec, Fixtures}
import models.Audience
import org.mockito.Matchers
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import play.api.libs.json.{JsArray, JsObject, JsString}
import services.dao._
import uk.ac.warwick.userlookup.UserLookupException
import warwick.sso._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

class AudienceServiceTest extends BaseSpec with MockitoSugar {
  trait Ctx {
    val webgroups: GroupService = mock[GroupService]
    val audienceDao: AudienceDao = mock[AudienceDao]
    val newsOptInDao: UserNewsOptInDao = mock[UserNewsOptInDao]
    val audienceLookupDao: AudienceLookupDao = mock[AudienceLookupDao]
    val userLookup: UserLookupService = mock[UserLookupService]
    val service = new AudienceServiceImpl(webgroups, audienceDao, newsOptInDao, audienceLookupDao, new MockDatabase, userLookup)


    def webgroupsIsEmpty(): Unit = {
      when(webgroups.getWebGroup(any())).thenReturn(Success(None)) // this webgroups is empty
      when(webgroups.getGroupsForQuery(any())).thenReturn(Success(Nil))
    }

    def audienceDaoIsEmpty(): Unit = {
      when(audienceLookupDao.resolveDepartment(any())).thenReturn(Future.successful(Seq()))
      when(audienceLookupDao.resolveUndergraduatesInDept(any(), any())).thenReturn(Future.successful(Seq()))
      when(audienceLookupDao.resolveUndergraduatesUniWide(any())).thenReturn(Future.successful(Seq()))
      when(audienceLookupDao.resolveTaughtPostgraduates(any())).thenReturn(Future.successful(Seq()))
      when(audienceLookupDao.resolveResearchPostgraduates(any())).thenReturn(Future.successful(Seq()))
      when(audienceLookupDao.resolveModule(any())).thenReturn(Future.successful(Seq()))
      when(audienceLookupDao.resolveSeminarGroup(any())).thenReturn(Future.successful(Seq()))
      when(audienceLookupDao.resolveRelationship(any(), any())).thenReturn(Future.successful(Seq()))
      when(audienceLookupDao.resolveStaff(any())).thenReturn(Future.successful(Seq()))
    }

    def newGroup(name: String, users: Seq[String], `type`:String="Arbitrary") =
      Group(GroupName(name), None, users.map(Usercode), Nil, `type`, null, null, restricted = false)

    // Welcome to Barry's World
    def webgroupsAllContainBarry(): Unit = {
      val barryGroup = newGroup("in-barry-world", Seq("cuddz"))
      when(webgroups.getWebGroup(any())).thenReturn(Success(Some(barryGroup)))
      when(webgroups.getGroupsForQuery(any())).thenReturn(Success(Seq(barryGroup)))
    }
  }

  import Audience._

  "AudienceService" should {

    "return an empty list for an empty audience" in new Ctx {
      service.resolve(Audience()).get must be (Set.empty)
    }

    "search for all research postgrads in WebGroups" in new Ctx {
      webgroupsIsEmpty()
      service.resolve(Audience(Seq(ResearchPostgrads))).get
      verify(webgroups).getWebGroup(GroupName("all-studenttype-postgraduate-research-ft"))
      verify(webgroups).getWebGroup(GroupName("all-studenttype-postgraduate-research-pt"))
      verifyNoMoreInteractions(webgroups)
      verifyZeroInteractions(audienceLookupDao)
    }

    "search for all taught postgrads in WebGroups" in new Ctx {
      webgroupsIsEmpty()
      service.resolve(Audience(Seq(TaughtPostgrads))).get
      verify(webgroups).getWebGroup(GroupName("all-studenttype-postgraduate-taught-ft"))
      verify(webgroups).getWebGroup(GroupName("all-studenttype-postgraduate-taught-pt"))
      verifyNoMoreInteractions(webgroups)
      verifyZeroInteractions(audienceLookupDao)
    }

    "search for first and second year undergrads in Tabula" in new Ctx {
      audienceDaoIsEmpty()
      import UndergradStudents._
      service.resolve(Audience(Seq(First, Second))).get
      verify(audienceLookupDao).resolveUndergraduatesUniWide(First)
      verify(audienceLookupDao).resolveUndergraduatesUniWide(Second)
      verifyNoMoreInteractions(audienceLookupDao)
      verifyZeroInteractions(webgroups)
    }

    "search for dept undergrads in Tabula" in new Ctx {
      audienceDaoIsEmpty()
      val deptCode = "AH"
      service.resolve(Audience(Seq(DepartmentAudience(deptCode, Seq(UndergradStudents.All))))).get
      verify(audienceLookupDao).resolveUndergraduatesInDept(deptCode, UndergradStudents.All)
      verifyNoMoreInteractions(audienceLookupDao)
      verifyZeroInteractions(webgroups)
    }

    "search for all teaching staff in WebGroups" in new Ctx {
      webgroupsIsEmpty()
      service.resolve(Audience(Seq(TeachingStaff))).get
      verify(webgroups).getWebGroup(GroupName("all-teaching"))
      verifyNoMoreInteractions(webgroups)
      verifyZeroInteractions(audienceLookupDao)
    }

    "search for all admin staff in WebGroups" in new Ctx {
      webgroupsIsEmpty()
      service.resolve(Audience(Seq(AdminStaff))).get
      verify(webgroups).getWebGroup(GroupName("all-teaching"))
      verify(webgroups).getWebGroup(GroupName("all-staff"))
      verifyNoMoreInteractions(webgroups)
      verifyZeroInteractions(audienceLookupDao)
    }

    "search for all staff in WebGroups" in new Ctx {
      webgroupsIsEmpty()
      service.resolve(Audience(Seq(Staff))).get
      verify(webgroups).getWebGroup(GroupName("all-staff"))
      verifyNoMoreInteractions(webgroups)
      verifyZeroInteractions(audienceLookupDao)
    }

    "search for specific group in WebGroups" in new Ctx {
      webgroupsIsEmpty()
      service.resolve(Audience(Seq(WebGroupAudience(GroupName("in-foo"))))).get
      verify(webgroups).getWebGroup(GroupName("in-foo"))
      verifyNoMoreInteractions(webgroups)
      verifyZeroInteractions(audienceLookupDao)
    }

    "search for all users in department" in new Ctx {
      val deptCode = "ch"
      when(audienceLookupDao.resolveDepartment(deptCode)).thenReturn(Future.successful(Seq(Usercode("cusfal"))))
      service.resolve(Audience(Seq(DepartmentAudience(deptCode, Seq(All))))).get
      verify(audienceLookupDao, times(1)).resolveDepartment(deptCode)
      verifyNoMoreInteractions(audienceLookupDao)
      verifyZeroInteractions(webgroups)
    }

    "search for all undergrads in department" in new Ctx {
      val deptCode = "ch"
      when(audienceLookupDao.resolveUndergraduatesInDept(deptCode, UndergradStudents.All)).thenReturn(Future.successful(Seq(Usercode("cusfal"))))
      service.resolve(Audience(Seq(DepartmentAudience(deptCode, Seq(UndergradStudents.All))))).get
      verify(audienceLookupDao, times(1)).resolveUndergraduatesInDept(deptCode, UndergradStudents.All)
      verifyNoMoreInteractions(audienceLookupDao)
      verifyZeroInteractions(webgroups)
    }

    "search for all PGRs in department" in new Ctx {
      val deptCode = "ch"
      when(audienceLookupDao.resolveResearchPostgraduates(deptCode)).thenReturn(Future.successful(Seq(Usercode("cusfal"))))
      service.resolve(Audience(Seq(DepartmentAudience(deptCode, Seq(ResearchPostgrads))))).get
      verify(audienceLookupDao, times(1)).resolveResearchPostgraduates(deptCode)
      verifyNoMoreInteractions(audienceLookupDao)
      verifyZeroInteractions(webgroups)
    }

    "search for all PGTs in department" in new Ctx {
      val deptCode = "ch"
      when(audienceLookupDao.resolveTaughtPostgraduates(deptCode)).thenReturn(Future.successful(Seq(Usercode("cusfal"))))
      service.resolve(Audience(Seq(DepartmentAudience(deptCode, Seq(TaughtPostgrads))))).get
      verify(audienceLookupDao, times(1)).resolveTaughtPostgraduates(deptCode)
      verifyNoMoreInteractions(audienceLookupDao)
      verifyZeroInteractions(webgroups)
    }

    "search for a module" in new Ctx {
      val moduleCode = "CH160"
      when(audienceLookupDao.resolveModule(moduleCode)).thenReturn(Future.successful(Seq(Usercode("cusfal"))))
      service.resolve(Audience(Seq(ModuleAudience(moduleCode)))).get
      verify(audienceLookupDao, times(1)).resolveModule(moduleCode)
      verifyNoMoreInteractions(audienceLookupDao)
      verifyZeroInteractions(webgroups)
    }

    "search for a seminar group" in new Ctx {
      val groupId = "1234"
      when(audienceLookupDao.resolveSeminarGroup(groupId)).thenReturn(Future.successful(Seq(Usercode("cusfal"))))
      service.resolve(Audience(Seq(SeminarGroupAudience(groupId)))).get
      verify(audienceLookupDao, times(1)).resolveSeminarGroup(groupId)
      verifyNoMoreInteractions(audienceLookupDao)
      verifyZeroInteractions(webgroups)
    }

    "search for a relationship" in new Ctx {
      val relationshipType = "personalTutor"
      val agentId = UniversityID("1234")
      when(audienceLookupDao.resolveRelationship(agentId, relationshipType)).thenReturn(Future.successful(Seq(Usercode("cusfal"))))
      service.resolve(Audience(Seq(RelationshipAudience(relationshipType, agentId)))).get
      verify(audienceLookupDao, times(1)).resolveRelationship(agentId, relationshipType)
      verifyNoMoreInteractions(audienceLookupDao)
      verifyZeroInteractions(webgroups)
    }

    "search for combination of audiences" in new Ctx {
      webgroupsIsEmpty()
      audienceDaoIsEmpty()
      service.resolve(Audience(Seq(
        WebGroupAudience(GroupName("in-winners")),
        WebGroupAudience(GroupName("in-losers")),
        ModuleAudience("CS102"),
        DepartmentAudience("CH", Seq(UndergradStudents.All)),
        DepartmentAudience("PH", Seq(UndergradStudents.All, Staff))
      ))).get
      verify(webgroups).getWebGroup(GroupName("in-winners"))
      verify(webgroups).getWebGroup(GroupName("in-losers"))
      verify(audienceLookupDao).resolveModule("CS102")
      verify(audienceLookupDao).resolveUndergraduatesInDept("CH", UndergradStudents.All)
      verify(audienceLookupDao).resolveUndergraduatesInDept("PH", UndergradStudents.All)
      verify(audienceLookupDao).resolveStaff("PH")
      verifyNoMoreInteractions(webgroups)
      verifyNoMoreInteractions(audienceLookupDao)
    }

    "deduplicate usercodes" in new Ctx {
      webgroupsAllContainBarry()
      val users: Set[Usercode] = service.resolve(Audience(Seq(
        ResearchPostgrads, TaughtPostgrads, AdminStaff
      ))).get

      users must be (Set(Usercode("cuddz")))
    }

    "combine opt-in components" in new Ctx {
      when(newsOptInDao.getUsercodes(Matchers.eq(LocationOptIn.CentralCampusResidences))(Matchers.any())).thenReturn(Set(
        Usercode("cusfal"),
        Usercode("cusebr"),
        Usercode("cuscao")
      ))

      val users: Set[Usercode] = service.resolve(Audience(Seq(
        UsercodesAudience(Set(Usercode("cusfal"), Usercode("cusebr"), Usercode("cusaab"))),
        LocationOptIn.CentralCampusResidences
      ))).get

      users must have size 2
      users must be (Set(Usercode("cusfal"), Usercode("cusebr")))
    }

    "validate both usercodes and university ids" in new Ctx {
      val codes = Seq(Usercode("cusebh"), Usercode("cusjau"))
      val validId = Usercode("0123456")
      val invalidId = Usercode("1234567")
      val ids = Seq(validId, invalidId)
      val codesAndIds: Seq[Usercode] = codes ++ ids

      when(userLookup.getUsers(any[Seq[Usercode]]))
        .thenReturn(Try(codes.map(c => c -> Fixtures.user.makeFoundUser(c.string)).toMap))
      when(userLookup.getUsers(ids.map(_.string).map(UniversityID), includeDisabled = false))
        .thenReturn(Try(Seq(UniversityID(validId.string) -> Fixtures.user.makeFoundUser(validId.string)).toMap))
      when(userLookup.getUsers(Seq.empty[UniversityID])).thenReturn(Failure(new UserLookupException))

      val actual = service.validateUsers(codesAndIds.toSet)

      actual must be (Left(Set(invalidId)))
    }

    "handle prepending 'u' to uni ids" in new Ctx {
      val validUsercode = Seq(Usercode("u1234567"))

      val bobsId = UniversityID("7654321")
      val bobsUsercode = Usercode("pacman")
      val bobsIdDisguisedAsUsercode = Seq(Usercode(s"u${bobsId.string}"))

      val codes: Seq[Usercode] = validUsercode ++ bobsIdDisguisedAsUsercode

      when(userLookup.getUsers(Seq.empty[UniversityID])).thenReturn(Failure[Map[UniversityID, User]](new Exception("epic fail")))
      when(userLookup.getUsers(codes))
        .thenReturn(Try(validUsercode.map(c => c -> Fixtures.user.makeFoundUser(c.string)).toMap))
      when(userLookup.getUsers(Seq(bobsId), includeDisabled = false))
        .thenReturn(Try(Seq(bobsId -> Fixtures.user.makeFoundUser(bobsUsercode.string)).toMap))

      val actual: Either[Set[Usercode], Set[Usercode]] = service.validateUsers(codes.toSet)

      actual must be (Right((validUsercode :+ bobsUsercode).toSet))
    }

    "serialize audience JSON" in new Ctx {
      private val universityWideGroups = service.audienceToJson(Audience(Seq(
        All,
        Staff,
        UndergradStudents.All,
        TaughtPostgrads,
        ResearchPostgrads
      )))("audience")("universityWide")("groups")
      universityWideGroups("All") mustBe JsString("undefined")
      universityWideGroups("Staff") mustBe JsString("undefined")
      universityWideGroups("TaughtPostgrads") mustBe JsString("undefined")
      universityWideGroups("ResearchPostgrads") mustBe JsString("undefined")
      universityWideGroups("undergraduates") mustBe JsString("UndergradStudents:All")

      private val undergraduateGroups = service.audienceToJson(Audience(Seq(
        UndergradStudents.First,
        UndergradStudents.Second,
        UndergradStudents.Final
      )))("audience")("universityWide")("groups")("undergraduates")
      undergraduateGroups("year")("UndergradStudents:First") mustBe JsString("undefined")
      undergraduateGroups("year")("UndergradStudents:Second") mustBe JsString("undefined")
      undergraduateGroups("year")("UndergradStudents:Final") mustBe JsString("undefined")

      when(audienceLookupDao.findModules("CH160")).thenReturn(Future.successful(Seq(LookupModule("CH160", "Module", "Description"))))
      when(audienceLookupDao.getSeminarGroupById("1234")).thenReturn(Future.successful(Some(LookupSeminarGroup("1234", "Group", "Description", "CH160"))))
      when(audienceLookupDao.findRelationships(UniversityID("1234567"))).thenReturn(Future.successful(Map(LookupRelationshipType("tutor", "tutor", "tutee") -> Seq(User.unknown(Usercode("cusfal"))))))
      when(userLookup.getUsers(Seq(UniversityID("1234567")))).thenReturn(Success(Map(UniversityID("1234567") -> User.unknown(Usercode("cusfal")))))
      private val otherAudience = service.audienceToJson(Audience(Seq(
        UsercodesAudience(Set(Usercode("cusfal"), Usercode("cuscao"))),
        ModuleAudience("CH160"),
        SeminarGroupAudience("1234"),
        RelationshipAudience("tutor", UniversityID("1234567")),
        ResidenceAudience(Residence.All)
      )))("audience")("universityWide")("groups")
      otherAudience("staffRelationships")(0)("value") mustBe JsString("1234567")
      otherAudience("seminarGroups")(0)("value") mustBe JsString("1234")
      otherAudience("modules")(0)("value") mustBe JsString("CH160")
      private val usercodes = otherAudience("listOfUsercodes").as[JsArray].value
      usercodes must contain (JsString("cusfal"))
      usercodes must contain (JsString("cuscao"))
    }
  }
}
