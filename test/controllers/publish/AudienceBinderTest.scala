package controllers.publish

import helpers.BaseSpec
import models.Audience
import models.Audience.{Residence, ResidenceAudience}
import models.publishing.PermissionScope.{AllDepartments, Departments}
import models.publishing.Publisher
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import play.api.data.FormError
import play.api.test.FakeRequest
import services.dao.{DepartmentInfo, DepartmentInfoDao}
import services.{AudienceService, PublisherService}
import warwick.sso.{AuthenticatedRequest, UniversityID, Usercode}

import scala.util.Try

class AudienceBinderTest extends BaseSpec with MockitoSugar with ScalaFutures {

  def mockPublisherService(deptPermissions: String*): PublisherService = {
    val publisherService = mock[PublisherService]
    when(publisherService.getPermissionScope(any[String]()))
      .thenReturn(
        if (deptPermissions.contains("**")) AllDepartments
        else Departments(deptPermissions)
      )
    publisherService
  }

  val defaultMockPublisherService: PublisherService = mockPublisherService("**")
  val publisher = Publisher("xyz", "Publisher", Some(1))
  implicit val defaultPublisherRequest: PublisherRequest[_] = new PublisherRequest(publisher, null, new AuthenticatedRequest(null, FakeRequest()))

  "AudienceBinder" should {

    "return Seq of Public when unbinding public Audience" in {
      val audience = Audience(Seq(Audience.PublicAudience))
      val audienceBinder: AudienceBinder = new AudienceBinder(null, null, defaultMockPublisherService)
      val result = audienceBinder.unbindAudience(audience).audience
      result mustBe Seq("Public")
    }

    "bind string Public to only Audience.Public" in {
      when(defaultMockPublisherService.getPermissionScope(any[String]())).thenReturn(AllDepartments)

      val audienceBinder: AudienceBinder = new AudienceBinder(null, null, defaultMockPublisherService)
      audienceBinder.bindAudience(AudienceData(Seq("Public"), null))(defaultPublisherRequest).futureValue mustBe Right(Audience.Public)
    }

    "bind single department single audience" in {

      val departmentCode = "AH"
      val audience = Seq(
        "Dept:TeachingStaff"
      )

      val audienceData = AudienceData(
        audience,
        Some(departmentCode) //Arden house
      )
      val departmentInfoDao = mock[DepartmentInfoDao]
      when(departmentInfoDao.allDepartments).thenReturn(Seq[DepartmentInfo](DepartmentInfo("AH", "AH", "AH", "AH", "AH", "X")))
      val audienceBinder = new AudienceBinder(departmentInfoDao, null, defaultMockPublisherService)
      audienceBinder.bindAudience(audienceData)(defaultPublisherRequest).futureValue mustBe Right(Audience(Seq(Audience.DepartmentAudience(departmentCode, Seq(Audience.TeachingStaff)))))

    }

    "bind multiple department audience" in {
      val departmentCode = "AH"
      val audience = Seq(
        "Dept:TeachingStaff",
        "Dept:TaughtPostgrads"
      )

      val audienceData = AudienceData(
        audience,
        Some(departmentCode) //Arden house
      )
      val departmentInfoDao = mock[DepartmentInfoDao]
      when(departmentInfoDao.allDepartments).thenReturn(Seq[DepartmentInfo](DepartmentInfo("AH", "AH", "AH", "AH", "AH", "X")))
      val audienceBinder = new AudienceBinder(departmentInfoDao, null, defaultMockPublisherService)

      audienceBinder.bindAudience(audienceData)(defaultPublisherRequest).futureValue mustBe Right(Audience(Seq(Audience.DepartmentAudience(departmentCode, Seq(Audience.TeachingStaff, Audience.TaughtPostgrads)))))

    }

    "bind single non-department audience" in {
      val audience = Seq(
        "TaughtPostgrads"
      )

      val audienceData = AudienceData(
        audience,
        None
      )

      val audienceBinder = new AudienceBinder(null, null, defaultMockPublisherService)
      audienceBinder.bindAudience(audienceData)(defaultPublisherRequest).futureValue mustBe Right(Audience(Seq(Audience.TaughtPostgrads)))
    }

    "bind multiple non-department audiences" in {
      val audience = Seq(
        "TeachingStaff",
        "TaughtPostgrads"
      )

      val audienceData = AudienceData(
        audience,
        None
      )

      val audienceBinder = new AudienceBinder(null, null, defaultMockPublisherService)
      audienceBinder.bindAudience(audienceData)(defaultPublisherRequest).futureValue mustBe Right(Audience(Seq(Audience.TeachingStaff, Audience.TaughtPostgrads)))
    }

    "bind multiple department and non-department audiences" in {
      val departmentCode = "AH"
      val audience = Seq(
        "TeachingStaff",
        "UndergradStudents:All",
        "Dept:TeachingStaff",
        "Dept:ResearchPostgrads"
      )

      val audienceData = AudienceData(
        audience,
        Some(departmentCode) //Arden house
      )
      val departmentInfoDao = mock[DepartmentInfoDao]
      when(departmentInfoDao.allDepartments).thenReturn(Seq(DepartmentInfo("AH", "AH", "AH", "AH", "AH", "X")))
      val audienceBinder = new AudienceBinder(departmentInfoDao, null, defaultMockPublisherService)

      audienceBinder.bindAudience(audienceData)(defaultPublisherRequest).futureValue mustBe Right(Audience(Seq(Audience.TeachingStaff, Audience.UndergradStudents.All, Audience.DepartmentAudience(departmentCode, Seq(Audience.TeachingStaff, Audience.ResearchPostgrads)))))

    }

    "not error when binding with empty WebGroup audience" in {
      val audience = Seq(
        "Staff",
        "WebGroup:"
      )

      val audienceData = AudienceData(
        audience,
        None
      )

      val departmentInfoDao = mock[DepartmentInfoDao]
      when(departmentInfoDao.allDepartments).thenReturn(Seq(DepartmentInfo("AH", "AH", "AH", "AH", "AH", "X")))
      val audienceBinder = new AudienceBinder(departmentInfoDao, null, defaultMockPublisherService)

      audienceBinder.bindAudience(audienceData)(defaultPublisherRequest).futureValue mustBe Right(Audience(Seq(Audience.Staff)))
    }

    "bind halls of residence audience data" in {
      val audienceData = AudienceData(Seq("hallsOfResidence:westwood","hallsOfResidence:claycroft"), None)

      val audienceBinder = new AudienceBinder(null, null, defaultMockPublisherService)
      audienceBinder.bindAudience(audienceData).futureValue mustBe Right(Audience(Seq(Residence.Westwood, Residence.Claycroft).map(ResidenceAudience)))
    }

    "raise error message when binding with invalid department code" in {

      val departmentCode = "AH"
      val audience = Seq(
        "TeachingStaff",
        "Dept:ResearchPostgrads"
      )

      val audienceData = AudienceData(
        audience,
        Some(departmentCode)
      )
      val departmentInfoDao = mock[DepartmentInfoDao]
      when(departmentInfoDao.allDepartments).thenReturn(Seq(DepartmentInfo("BB", "BB", "BB", "BB", "BB", "X")))
      val audienceBinder = new AudienceBinder(departmentInfoDao, null, defaultMockPublisherService)

      audienceBinder.bindAudience(audienceData)(defaultPublisherRequest).futureValue mustBe Left(Seq(FormError("department", "error.department.invalid")))
    }

    "raise error message when binding with invalid department audience" in {

      val departmentCode = "AH"
      // this value does not match any defined dept subset types so we attempt to validate it as a Usercode
      val unrecognisedAudience = "Dept:TeachingApple"
      val audience = Seq(
        unrecognisedAudience,
        "Dept:ResearchPostgrads"
      )

      val audienceData = AudienceData(
        audience,
        Some(departmentCode) //Arden house
      )
      val departmentInfoDao = mock[DepartmentInfoDao]
      when(departmentInfoDao.allDepartments).thenReturn(Seq(DepartmentInfo("AH", "AH", "AH", "AH", "AH", "X")))

      val audienceService = mock[AudienceService]
      when(audienceService.validateUsers(Set(Usercode("TeachingApple")))).thenReturn(Left(Set(Usercode("TeachingApple"))))

      val audienceBinder = new AudienceBinder(departmentInfoDao, audienceService, defaultMockPublisherService)

      audienceBinder.bindAudience(audienceData)(defaultPublisherRequest).futureValue mustBe
        Left(Seq(FormError("audience.usercodes", Seq("error.audience.usercodes.invalid"), Seq("TeachingApple"))))
    }

    "raise error message when binding with invalid non-department audience" in {
      val unrecognisedAudience = "TeachingApple" // if audience component contains no ":" then we userLookup to see if it's real
      val audience = Seq(
        unrecognisedAudience,
        "ResearchPostgrads"
      )

      val audienceData = AudienceData(
        audience,
        None
      )

      val mockAudienceService = mock[AudienceService]
      when(mockAudienceService.validateUsers(Set(Usercode(unrecognisedAudience)))).thenReturn(Left(Set(Usercode(unrecognisedAudience))))

      val audienceBinder = new AudienceBinder(null, mockAudienceService, defaultMockPublisherService)

      audienceBinder.bindAudience(audienceData)(defaultPublisherRequest).futureValue mustBe Left(Seq(FormError("audience.usercodes", "error.audience.usercodes.invalid", Seq(unrecognisedAudience))))
    }


    "raise error message when binding with empty audiences" in {
      val audience = Seq("")
      val audienceData = AudienceData(
        audience,
        None
      )
      val audienceBinder = new AudienceBinder(null, null, defaultMockPublisherService)
      audienceBinder.bindAudience(audienceData)(defaultPublisherRequest).futureValue mustBe Left(Seq(FormError("audience", "error.audience.invalid", Seq("")), FormError("audience", "error.audience.empty")))
    }

    "raise error message if more than restricted recipients" in {
      val audienceData = AudienceData(Seq("TaughtPostgrads"), None)
      val publisher = Publisher("xyz", "Publisher", Some(1))

      val mockAudienceService = mock[AudienceService]
      when(mockAudienceService.resolve(Audience(Seq(Audience.ComponentParameter.unapply("TaughtPostgrads").get)))).thenReturn(Try(Set.apply[Usercode](Usercode("a"), Usercode("b"))))
      val audienceBinder = new AudienceBinder(null, mockAudienceService, defaultMockPublisherService)
      val result = audienceBinder.bindAudience(audienceData, restrictedRecipients = true)(new PublisherRequest(publisher, null, new AuthenticatedRequest(null, FakeRequest()))).futureValue
      result mustBe Left(Seq(FormError("audience", "error.audience.tooMany", Seq(1))))
    }

    "unbind module audience" in {
      val audience = Audience(Seq(Audience.ModuleAudience("CH160")))
      val audienceBinder: AudienceBinder = new AudienceBinder(null, null, defaultMockPublisherService)
      val result = audienceBinder.unbindAudience(audience).audience
      result mustBe Seq("Module:CH160")
    }

    "unbind seminar audience" in {
      val audience = Audience(Seq(Audience.SeminarGroupAudience("group-id")))
      val audienceBinder: AudienceBinder = new AudienceBinder(null, null, defaultMockPublisherService)
      val result = audienceBinder.unbindAudience(audience).audience
      result mustBe Seq("SeminarGroup:group-id")
    }

    "unbind relationship" in {
      val audience = Audience(Seq(Audience.RelationshipAudience("personalTutor", UniversityID("1234"))))
      val audienceBinder: AudienceBinder = new AudienceBinder(null, null, defaultMockPublisherService)
      val result = audienceBinder.unbindAudience(audience).audience
      result mustBe Seq("Relationship:personalTutor:1234")
    }

    "bind multiple UndergradStudents subsets" in {
      val departmentCode = "AH"
      val audience = Seq(
        "Dept:UndergradStudents:First",
        "Dept:UndergradStudents:Second"
      )

      val audienceData = AudienceData(
        audience,
        Some(departmentCode) //Arden house
      )
      val departmentInfoDao = mock[DepartmentInfoDao]
      when(departmentInfoDao.allDepartments).thenReturn(Seq(DepartmentInfo("AH", "AH", "AH", "AH", "AH", "X")))
      val audienceBinder = new AudienceBinder(departmentInfoDao, null, defaultMockPublisherService)

      audienceBinder.bindAudience(audienceData)(defaultPublisherRequest).futureValue mustBe Right(Audience(Seq(Audience.DepartmentAudience(departmentCode, Seq(Audience.UndergradStudents.First, Audience.UndergradStudents.Second)))))
    }
  }
}
