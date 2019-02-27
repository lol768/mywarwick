package controllers.api

import helpers.{BaseSpec, WithActorSystem}
import models.{API, Tile, TileInstance}
import org.scalatest.mockito.MockitoSugar
import org.mockito.ArgumentMatchers.{eq => isEq, _}
import org.mockito.Mockito._
import play.api.libs.json.{JsArray, JsDefined, Json}
import play.api.mvc._
import play.api.test.Helpers._
import play.api.test.FakeRequest
import services.{SecurityService, SecurityServiceImpl, TileContentService, TileService}
import warwick.sso._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class EventsMergeControllerTest extends BaseSpec with MockitoSugar with Results with WithActorSystem {
  val fox: User = Users.create(usercode = Usercode("in-reynard-fox"))
  val FakeLoginUrl = "https://app.example.com/login"

  val mockSSOClient = new MockSSOClient(new LoginContext {
    override val user: Option[User] = Some(fox)
    override val actualUser: Option[User] = None

    override def loginUrl(target: Option[String]): String = FakeLoginUrl

    override def userHasRole(role: RoleName) = true

    override def actualUserHasRole(role: RoleName) = true
  })

  "EventsMergeControllerTest" should {

    val tileService: TileService = mock[TileService]
    val tileContentService: TileContentService = mock[TileContentService]
    val security: SecurityService = new SecurityServiceImpl(mockSSOClient, mock[BasicAuth], PlayBodyParsers())
    val controller = new EventsMergeController(tileService, tileContentService, security)

    val uniEventTile = TileInstance(Tile("uni-events", "type", 0, None, "title", None, 0), None, false)
    val calendarTile = TileInstance(Tile("calendar", "type", 0, None, "title", None, 0), None, false)
    val sportsTile = TileInstance(Tile("sports", "type", 0, None, "title", None, 0), None, false)
    val timetableTile = TileInstance(Tile("timetable", "type", 0, None, "title", None, 0), None, false)

    val tileInstances: Seq[TileInstance] = Seq(uniEventTile, calendarTile, sportsTile, timetableTile)
    when(tileService.getTilesForUser(any())).thenReturn(tileInstances)


    val calendarJson = Json.arr(Json.obj("calendarOne" -> 1, "start" -> "2"))
    when(tileContentService.getTileContent(any(), isEq(calendarTile)))
      .thenReturn(Future.successful(API.Success(data = Json.obj(
        "items" -> calendarJson
      ))))
    val timetableJson = Json.arr(Json.obj("timetableOne" -> 1, "start" -> "4"))
    when(tileContentService.getTileContent(any(), isEq(timetableTile)))
      .thenReturn(Future.successful(API.Success(data = Json.obj(
        "items" -> timetableJson
      ))))
    val sportsJson = Json.arr(Json.obj("sportsOne" -> 1, "start" -> "3"))
    when(tileContentService.getTileContent(any(), isEq(sportsTile)))
      .thenReturn(Future.successful(API.Success(data = Json.obj(
        "items" -> sportsJson
      ))))
    val uniEventJson = Json.arr(Json.obj("uniEventOne" -> 1, "start" -> "1"))
    when(tileContentService.getTileContent(any(), isEq(uniEventTile)))
      .thenReturn(Future.successful(API.Success(data = Json.obj(
        "items" -> uniEventJson
      ))))

    "merge and sort items from all calendar sources" in {
      val request = FakeRequest().withJsonBody(Json.obj(
        "calendars" -> Json.obj(
          "uni-events" -> true,
          "calendar" -> true,
          "sports" -> true,
          "timetable" -> true
        )
      ))

      val result = controller.index(request)
      status(result) must be(200)
      (contentAsJson(result) \ "data" \ "items") must be(
        JsDefined(Seq(uniEventJson, calendarJson, sportsJson, timetableJson).reduce(_ ++ _).as[JsArray])
      )
    }

    "merge and sort items from selected calendar sources" in {
      val request = FakeRequest().withJsonBody(Json.obj(
        "calendars" -> Json.obj(
          "uni-events" -> true,
          "calendar" -> false,
          "sports" -> true,
          "timetable" -> false
        )
      ))

      val result = controller.index(request)
      status(result) must be(200)
      (contentAsJson(result) \ "data" \ "items") must be(
        JsDefined(Seq(uniEventJson, sportsJson).reduce(_ ++ _).as[JsArray])
      )
    }

    "return no items when no calendar sources selected" in {
      val request = FakeRequest().withJsonBody(Json.obj(
        "calendars" -> Json.obj(
          "uni-events" -> false,
          "calendar" -> false,
          "sports" -> false,
          "timetable" -> false
        )
      ))

      val result = controller.index(request)
      status(result) must be(200)
      (contentAsJson(result) \ "data" \ "items") must be(
        JsDefined(Json.arr())
      )
    }
  }
}
