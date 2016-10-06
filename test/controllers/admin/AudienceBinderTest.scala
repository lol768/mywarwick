package controllers.admin

import models.Audience
import org.scalatestplus.play.PlaySpec

class AudienceBinderTest extends PlaySpec {

  "AudienceBinderTest" should {

    "return Seq of Public when unbinding public Audience" in {
      val audience = Audience(Seq(Audience.PublicAudience))
      val audienceBinder: AudienceBinder = new AudienceBinder(null)
      val result = audienceBinder.unbindAudience(audience).audience
      result mustBe Seq("Public")
    }
  }
}
