package controllers

import org.scalatest.mockito.MockitoSugar
import helpers.{BaseSpec, OneStartAppPerSuite}
import play.api.mvc._
import play.api.test.Helpers._
import play.api.test._

class HomeControllerTest extends BaseSpec with MockitoSugar with Results with OneStartAppPerSuite {
  val controller: HomeController = get[HomeController]
  controller.setControllerComponents(get[ControllerComponents])

  "ApplicationController#index" should {
    "render" in {
      val result = controller.index(FakeRequest())
      status(result) must be(200)
      contentAsString(result) must include("id=\"app-container\"")
      contentAsString(result) must include("data-features=\"{&quot;news&quot;:false,&quot;updateTileEditUI&quot;:false}\"")
    }
  }
}
