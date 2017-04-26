package helpers

import akka.stream.ActorMaterializer

class TestApplicationsSpec extends BaseSpec {

  "TestApplications.minimal" should {
    "work out of the box" in {
      val app = TestApplications.minimal()
      app.materializer mustBe an[ActorMaterializer]
    }
  }

}
