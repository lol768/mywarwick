package controllers.publish

import models.Audience
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import helpers.BaseSpec
import models.publishing.Publisher
import play.api.data.FormError
import play.api.test.FakeRequest
import services.AudienceService
import services.dao.{DepartmentInfo, DepartmentInfoDao}
import warwick.sso.AuthenticatedRequest

import scala.util.Try

class AudienceBinderTest extends BaseSpec with MockitoSugar with ScalaFutures {

  "AudienceBinder" should {

    "return Seq of Public when unbinding public Audience" in {
      val audience = Audience(Seq(Audience.PublicAudience))
      val audienceBinder: AudienceBinder = new AudienceBinder(null, null)
      val result = audienceBinder.unbindAudience(audience).audience
      result mustBe Seq("Public")
    }

    "bind string Public to only Audience.Public" in {
      val audienceBinder: AudienceBinder = new AudienceBinder(null, null)
      audienceBinder.bindAudience(AudienceData(Seq("Public"), null))(null).futureValue mustBe Right(Audience.Public)
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
      when(departmentInfoDao.allDepartments).thenReturn(Seq[DepartmentInfo](DepartmentInfo("AH", "AH", "AH", "AH", "AH")))
      val audienceBinder = new AudienceBinder(departmentInfoDao, null)
      audienceBinder.bindAudience(audienceData)(null).futureValue mustBe Right(Audience(Seq(Audience.DepartmentAudience(departmentCode, Seq(Audience.TeachingStaff)))))

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
      when(departmentInfoDao.allDepartments).thenReturn(Seq[DepartmentInfo](DepartmentInfo("AH", "AH", "AH", "AH", "AH")))
      val audienceBinder = new AudienceBinder(departmentInfoDao, null)

      audienceBinder.bindAudience(audienceData)(null).futureValue mustBe Right(Audience(Seq(Audience.DepartmentAudience(departmentCode, Seq(Audience.TeachingStaff, Audience.TaughtPostgrads)))))

    }

    "bind single non-department audience" in {
      val audience = Seq(
        "TaughtPostgrads"
      )

      val audienceData = AudienceData(
        audience,
        None
      )

      val audienceBinder = new AudienceBinder(null, null)
      audienceBinder.bindAudience(audienceData)(null).futureValue mustBe Right(Audience(Seq(Audience.TaughtPostgrads)))
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

      val audienceBinder = new AudienceBinder(null, null)
      audienceBinder.bindAudience(audienceData)(null).futureValue mustBe Right(Audience(Seq(Audience.TeachingStaff, Audience.TaughtPostgrads)))
    }

    "bind multiple department and non-department audiences" in {
      val departmentCode = "AH"
      val audience = Seq(
        "TeachingStaff",
        "UndergradStudents",
        "Dept:TeachingStaff",
        "Dept:ResearchPostgrads"
      )

      val audienceData = AudienceData(
        audience,
        Some(departmentCode) //Arden house
      )
      val departmentInfoDao = mock[DepartmentInfoDao]
      when(departmentInfoDao.allDepartments).thenReturn(Seq(DepartmentInfo("AH", "AH", "AH", "AH", "AH")))
      val audienceBinder = new AudienceBinder(departmentInfoDao, null)

      audienceBinder.bindAudience(audienceData)(null).futureValue mustBe Right(Audience(Seq(Audience.TeachingStaff, Audience.UndergradStudents, Audience.DepartmentAudience(departmentCode, Seq(Audience.TeachingStaff, Audience.ResearchPostgrads)))))

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
      when(departmentInfoDao.allDepartments).thenReturn(Seq(DepartmentInfo("AH", "AH", "AH", "AH", "AH")))
      val audienceBinder = new AudienceBinder(departmentInfoDao, null)

      audienceBinder.bindAudience(audienceData)(null).futureValue mustBe Right(Audience(Seq(Audience.Staff)))
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
      when(departmentInfoDao.allDepartments).thenReturn(Seq(DepartmentInfo("BB", "BB", "BB", "BB", "BB")))
      val audienceBinder = new AudienceBinder(departmentInfoDao, null)

      audienceBinder.bindAudience(audienceData)(null).futureValue mustBe Left(Seq(FormError("department", "error.department.invalid")))
    }

    "raise error message when binding with invalid department audience" in {

      val departmentCode = "AH"
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
      when(departmentInfoDao.allDepartments).thenReturn(Seq(DepartmentInfo("AH", "AH", "AH", "AH", "AH")))
      val audienceBinder = new AudienceBinder(departmentInfoDao, null)

      audienceBinder.bindAudience(audienceData)(null).futureValue mustBe Left(Seq(FormError("audience", "error.audience.invalid", Seq(unrecognisedAudience))))
    }

    "raise error message when binding with invalid non-department audience" in {
      val unrecognisedAudience = "TeachingApple"
      val audience = Seq(
        unrecognisedAudience,
        "ResearchPostgrads"
      )

      val audienceData = AudienceData(
        audience,
        None
      )

      val audienceBinder = new AudienceBinder(null, null)

      audienceBinder.bindAudience(audienceData)(null).futureValue mustBe Left(Seq(FormError("audience", "error.audience.invalid", Seq(unrecognisedAudience))))
    }


    "raise error message when binding with empty audiences" in {
      val audience = Seq("")
      val audienceData = AudienceData(
        audience,
        None
      )
      val audienceBinder = new AudienceBinder(null, null)
      audienceBinder.bindAudience(audienceData)(null).futureValue mustBe Left(Seq(FormError("audience", "error.audience.invalid", Seq("")), FormError("audience", "error.audience.empty")))
    }

    "raise error message if more than restricted recipients" in {
      val audienceData = AudienceData(Seq("TaughtPostgrads"), None)
      val publisher = Publisher("xyz", "Publisher", Some(1))

      val mockAudienceService = mock[AudienceService]
      when(mockAudienceService.resolve(Audience(Seq(Audience.ComponentParameter.unapply("TaughtPostgrads").get)))).thenReturn(Try(Seq(null, null)))
      val audienceBinder = new AudienceBinder(null, mockAudienceService)
      val result = audienceBinder.bindAudience(audienceData, restrictedRecipients = true)(new PublisherRequest(publisher, null, new AuthenticatedRequest(null, null))).futureValue
      result mustBe Left(Seq(FormError("audience", "error.audience.tooMany", Seq(1))))
    }

  }
}
