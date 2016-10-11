package controllers.admin

import models.Audience
import models.Audience.{Staff, UndergradStudents}
import org.mockito.Matchers.any
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.data.FormError
import services.dao.{DepartmentInfo, DepartmentInfoDao}
import play.api.test.Helpers._
import play.api.test._

import scala.concurrent.Future

class AudienceBinderTest extends PlaySpec with MockitoSugar with ScalaFutures {

  "AudienceBinder" should {

    "return Seq of Public when unbinding public Audience" in {
      val audience = Audience(Seq(Audience.PublicAudience))
      val audienceBinder: AudienceBinder = new AudienceBinder(null)
      val result = audienceBinder.unbindAudience(audience).audience
      result mustBe Seq("Public")
    }

    "bind string Public to only Audience.Public" in {
      val audienceBinder: AudienceBinder = new AudienceBinder(null)
      audienceBinder.bindAudience(AudienceData(Seq("Public"), null)).futureValue mustBe Right(Audience.Public)
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
      val departmentInforDao = mock[DepartmentInfoDao]
      when(departmentInforDao.allDepartments).thenReturn(Seq[DepartmentInfo](DepartmentInfo("AH", "AH", "AH", "AH", "AH")))
      val audienceBinder = new AudienceBinder(departmentInforDao)
      audienceBinder.bindAudience(audienceData).futureValue mustBe Right(Audience(Seq(Audience.DepartmentAudience(departmentCode, Seq(Audience.TeachingStaff)))))

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
      val departmentInforDao = mock[DepartmentInfoDao]
      when(departmentInforDao.allDepartments).thenReturn(Seq[DepartmentInfo](DepartmentInfo("AH", "AH", "AH", "AH", "AH")))
      val audienceBinder = new AudienceBinder(departmentInforDao)

      audienceBinder.bindAudience(audienceData).futureValue mustBe Right(Audience(Seq(Audience.DepartmentAudience(departmentCode, Seq(Audience.TeachingStaff, Audience.TaughtPostgrads)))))

    }

    "bind single non-department audience" in {
      val audience = Seq(
        "TaughtPostgrads"
      )

      val audienceData = AudienceData(
        audience,
        None
      )

      val audienceBinder = new AudienceBinder(null)
      audienceBinder.bindAudience(audienceData).futureValue mustBe Right(Audience(Seq(Audience.TaughtPostgrads)))
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

      val audienceBinder = new AudienceBinder(null)
      audienceBinder.bindAudience(audienceData).futureValue mustBe Right(Audience(Seq(Audience.TeachingStaff, Audience.TaughtPostgrads)))
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
      val departmentInforDao = mock[DepartmentInfoDao]
      when(departmentInforDao.allDepartments).thenReturn(Seq(DepartmentInfo("AH", "AH", "AH", "AH", "AH")))
      val audienceBinder = new AudienceBinder(departmentInforDao)

      audienceBinder.bindAudience(audienceData).futureValue mustBe Right(Audience(Seq(Audience.TeachingStaff, Audience.UndergradStudents, Audience.DepartmentAudience(departmentCode, Seq(Audience.TeachingStaff, Audience.ResearchPostgrads)))))

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
      val departmentInforDao = mock[DepartmentInfoDao]
      when(departmentInforDao.allDepartments).thenReturn(Seq(DepartmentInfo("BB", "BB", "BB", "BB", "BB")))
      val audienceBinder = new AudienceBinder(departmentInforDao)

      audienceBinder.bindAudience(audienceData).futureValue mustBe Left(Seq(FormError("department", "error.department.invalid")))
    }

    "raise error message when binding with invalid department audience" in {

      val departmentCode = "AH"
      val unreconisedAudience = "Dept:TeachingApple"
      val audience = Seq(
        unreconisedAudience,
        "Dept:ResearchPostgrads"
      )

      val audienceData = AudienceData(
        audience,
        Some(departmentCode) //Arden house
      )
      val departmentInforDao = mock[DepartmentInfoDao]
      when(departmentInforDao.allDepartments).thenReturn(Seq(DepartmentInfo("AH", "AH", "AH", "AH", "AH")))
      val audienceBinder = new AudienceBinder(departmentInforDao)

      audienceBinder.bindAudience(audienceData).futureValue mustBe Left(Seq(FormError("audience", "error.audience.invalid", unreconisedAudience)))
    }

    "raise error message when binding with invalid non-department audience" in {
      val unreconisedAudience = "TeachingApple"
      val audience = Seq(
        unreconisedAudience,
        "ResearchPostgrads"
      )

      val audienceData = AudienceData(
        audience,
        None
      )

      val audienceBinder = new AudienceBinder(null)

      audienceBinder.bindAudience(audienceData).futureValue mustBe Left(Seq(FormError("audience", "error.audience.invalid", unreconisedAudience)))
    }


    "raise error message when binding with empty audiences" in {
      val audience = Seq("")
      val audienceData = AudienceData(
        audience,
        None
      )
      val audienceBinder = new AudienceBinder(null)
      audienceBinder.bindAudience(audienceData).futureValue mustBe Left(Seq(FormError("audience", "error.audience.invalid") ,FormError("audience", "error.audience.empty")))
    }

  }
}
