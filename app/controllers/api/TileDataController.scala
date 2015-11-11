package controllers.api

import java.util.UUID

import com.google.inject.Inject
import org.joda.time.DateTime
import play.api.libs.json._
import play.api.mvc.{Action, Controller}
import warwick.sso.SSOClient

class TileDataController @Inject()(
  ssoClient: SSOClient
) extends Controller {

  val tileData = JsArray(Seq(
    JsObject(Seq(
      "key" -> JsString("mail"),
      "type" -> JsString("list"),
      "title" -> JsString("Mail"),
      "href" -> JsString("http://warwick.ac.uk/mymail"),
      "backgroundColor" -> JsString("#0078d7"),
      "icon" -> JsString("envelope-o"),
      "word" -> JsString("unread"),
      "size" -> JsString("wide"),
      "items" -> JsArray(Seq(
        JsObject(Seq(
          "key" -> JsNumber(1),
          "title" -> JsString("Christelle Evaert"),
          "text" -> JsString("Departmental meeting cancelled"),
          "date" -> JsString(new DateTime(2015, 3, 24, 11, 11).toString())
        )),
        JsObject(Seq(
          "key" -> JsNumber(2),
          "title" -> JsString("IT Service Desk"),
          "text" -> JsString("Emergency RFC"),
          "date" -> JsString(new DateTime(2015, 3, 25, 3, 57).toString())
        )),
        JsObject(Seq(
          "key" -> JsNumber(3),
          "title" -> JsString("Linda Squirrel"),
          "text" -> JsString("IT Induction Day reminder"),
          "date" -> JsString(new DateTime(2015, 3, 27, 22, 7).toString())
        ))

      ))
    )),

    JsObject(Seq(
      "key" -> JsString("tabula"),
      "type" -> JsString("text"),
      "title" -> JsString("Tabula"),
      "href" -> JsString("https://tabula.warwick.ac.uk"),
      "backgroundColor" -> JsString("#239b92"),
      "icon" -> JsString("cog"),
      "items" -> JsArray(Seq(
        JsObject(Seq(
          "key" -> JsNumber(1),
          "callout" -> JsNumber(3),
          "text" -> JsString("actions required")
        ))
      ))
    )),

    JsObject(Seq(
      "key" -> JsString("live - departures"),
      "type" -> JsString("text"),
      "title" -> JsString("Live Departures"),
      "href" -> JsString("http://warwick.ac.uk/insite/kcm"),
      "backgroundColor" -> JsString("#ef4050"),
      "icon" -> JsString("bus"),
      "items" -> JsArray(Seq(
        JsObject(Seq(
          "key" -> JsNumber(1),
          "callout" -> JsString("11:00"),
          "text" -> JsString("Express to Hogwarts")
        )),
        JsObject(Seq(
          "key" -> JsNumber(2),
          "callout" -> JsString("18:01"),
          "text" -> JsString("11 to Coventry")
        ))
      ))
    )),

    JsObject(Seq(
      "key" -> JsString("modules"),
      "type" -> JsString("list"),
      "title" -> JsString("My Modules"),
      "icon" -> JsString("mortar-board"),
      "items" -> JsArray(Seq(
        JsObject(Seq(
          "key" -> JsNumber(1),
          "href" -> JsString("http://warwick.ac.uk/cs118"),
          "text" -> JsString("CS118 Programming for Computer Scientists")
        )),
        JsObject(Seq(
          "key" -> JsNumber(2),
          "href" -> JsString("http://warwick.ac.uk/cs256"),
          "text" -> JsString("CS256 Functional Programming")
        )),
        JsObject(Seq(
          "key" -> JsNumber(3),
          "href" -> JsString("http://warwick.ac.uk/cs324"),
          "text" -> JsString("CS324 Computer Graphics")
        ))
      ))
    ))
  ))

  def requestTileData = ssoClient.Lenient { request =>
    Ok(Json.toJson(
      "type" -> "tiles",
      "tiles" -> Json.stringify(tileData)
    ))
  }
}
