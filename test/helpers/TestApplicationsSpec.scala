package helpers

import akka.stream.ActorMaterializer
import org.scalatestplus.play.PlaySpec

class TestApplicationsSpec extends PlaySpec {

  "TestApplications.minimal" should {
    "work out of the box" in {
      val app = TestApplications.minimal()
      app.materializer mustBe an[ActorMaterializer]
    }
  }

}
