package services

import helpers.{BaseSpec, Fixtures}
import models.Audience
import org.mockito.Matchers
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import services.dao.{AudienceDao, AudienceLookupDao, UserNewsOptInDao}
import warwick.sso._

import scala.concurrent.Future
import scala.util.{Success, Try}

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
      when(audienceLookupDao.resolveUndergraduates(any())).thenReturn(Future.successful(Seq()))
      when(audienceLookupDao.resolveTaughtPostgraduates(any())).thenReturn(Future.successful(Seq()))
      when(audienceLookupDao.resolveResearchPostgraduates(any())).thenReturn(Future.successful(Seq()))
      when(audienceLookupDao.resolveTeachingStaff(any())).thenReturn(Future.successful(Seq()))
      when(audienceLookupDao.resolveAdminStaff(any())).thenReturn(Future.successful(Seq()))
      when(audienceLookupDao.resolveModule(any())).thenReturn(Future.successful(Seq()))
      when(audienceLookupDao.resolveSeminarGroup(any())).thenReturn(Future.successful(Seq()))
      when(audienceLookupDao.resolveRelationship(any(), any())).thenReturn(Future.successful(Seq()))
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

    "search for all undergrads in WebGroups" in new Ctx {
      webgroupsIsEmpty()
      service.resolve(Audience(Seq(UndergradStudents))).get
      verify(webgroups).getWebGroup(GroupName("all-studenttype-undergraduate-full-time"))
      verify(webgroups).getWebGroup(GroupName("all-studenttype-undergraduate-part-time"))
      verifyNoMoreInteractions(webgroups)
      verifyZeroInteractions(audienceLookupDao)
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
      when(audienceLookupDao.resolveUndergraduates(deptCode)).thenReturn(Future.successful(Seq(Usercode("cusfal"))))
      service.resolve(Audience(Seq(DepartmentAudience(deptCode, Seq(UndergradStudents))))).get
      verify(audienceLookupDao, times(1)).resolveUndergraduates(deptCode)
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

    "search for all teaching staff in department" in new Ctx {
      val deptCode = "ch"
      when(audienceLookupDao.resolveTeachingStaff(deptCode)).thenReturn(Future.successful(Seq(Usercode("cusfal"))))
      service.resolve(Audience(Seq(DepartmentAudience(deptCode, Seq(TeachingStaff))))).get
      verify(audienceLookupDao, times(1)).resolveTeachingStaff(deptCode)
      verifyNoMoreInteractions(audienceLookupDao)
      verifyZeroInteractions(webgroups)
    }

    "search for all admin staff in department" in new Ctx {
      val deptCode = "ch"
      when(audienceLookupDao.resolveAdminStaff(deptCode)).thenReturn(Future.successful(Seq(Usercode("cusfal"))))
      service.resolve(Audience(Seq(DepartmentAudience(deptCode, Seq(AdminStaff))))).get
      verify(audienceLookupDao, times(1)).resolveAdminStaff(deptCode)
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
        DepartmentAudience("CH", Seq(UndergradStudents)),
        DepartmentAudience("PH", Seq(UndergradStudents, TeachingStaff))
      ))).get
      verify(webgroups).getWebGroup(GroupName("in-winners"))
      verify(webgroups).getWebGroup(GroupName("in-losers"))
      verify(audienceLookupDao).resolveModule("CS102")
      verify(audienceLookupDao).resolveUndergraduates("CH")
      verify(audienceLookupDao).resolveUndergraduates("PH")
      verify(audienceLookupDao).resolveTeachingStaff("PH")
      verifyNoMoreInteractions(webgroups)
      verifyNoMoreInteractions(audienceLookupDao)
    }

    "deduplicate usercodes" in new Ctx {
      webgroupsAllContainBarry()
      val users: Set[Usercode] = service.resolve(Audience(Seq(
        ResearchPostgrads, TaughtPostgrads, UndergradStudents
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
      when(userLookup.getUsers(ids.map(_.string).map(UniversityID), false))
        .thenReturn(Try(Seq(UniversityID(validId.string) -> Fixtures.user.makeFoundUser(validId.string)).toMap))

      val actual = service.validateUsercodes(codesAndIds.toSet)
      verify(userLookup, times(1)).getUsers(ids.map(u => UniversityID(u.string)))
      verify(userLookup, times(1)).getUsers(codesAndIds)

      actual must be (Left(Set(invalidId)))
    }
  }
}
