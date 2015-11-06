package controllers

import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.mvc._
import play.api.test.Helpers._
import play.api.test._
import warwick.sso._

class ApplicationControllerTest extends PlaySpec with MockitoSugar with Results {
  /**
    * This is quite verbose for a controller setup. We'd move some of this
    * SSOClient creation out to a helper, because we're likely to want the same
    * sort of stuff in any non-public controller.
    */
  val ron = Some(Users.create(
    usercode = Usercode("ron")
  ))
  val ssoClient = new MockSSOClient(new LoginContext {
    override val user: Option[User] = ron
    override val actualUser: Option[User] = None

    override def loginUrl(target: Option[String]): String = "https://app.example.com/login"
  })
  val controller = new ApplicationController(ssoClient)

  "ApplicationController#index" should {
    "render" in {
      val result = controller.index(FakeRequest())
      status(result) must be(200)
      contentAsString(result) must include("warwick")
    }
  }
}
