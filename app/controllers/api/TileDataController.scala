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
//          "key" -> JsNumber(1),
          "key" -> JsString(UUID.randomUUID().toString),
          "title" -> JsString("Christelle Evaert"),
          "text" -> JsString("Departmental meeting cancelled"),
          "date" -> JsString(new DateTime(2015, 3, 24, 11, 11).toString())
        )),
        JsObject(Seq(
//          "key" -> JsNumber(2),
          "key" -> JsString(UUID.randomUUID().toString),
          "title" -> JsString("IT Service Desk"),
          "text" -> JsString("Emergency RFC"),
          "date" -> JsString(new DateTime(2015, 3, 25, 3, 57).toString())
        )),
        JsObject(Seq(
//          "key" -> JsNumber(3),
          "key" -> JsString(UUID.randomUUID().toString),
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
      "backgroundColor" -> JsString("# 239 b92"),
      "icon" -> JsString("cog"),
      "callout" -> JsNumber(3),
      "text" -> JsString("actions required ")
    )),

    JsObject(Seq(
      "key" -> JsString("live - departures"),
      "type" -> JsString("text"),
      "title" -> JsString("Live Departures"),
      "href" -> JsString("http://warwick.ac.uk/insite/kcm"),
      "backgroundColor" -> JsString("# ef4050"),
      "icon" -> JsString("bus"),
      "callout" -> JsString(" 17: 52"),
      "text" -> JsString("U1 to Leamington")
    )),

    JsObject(Seq(
      "key" -> JsString("modules"),
      "type" -> JsString("list"),
      "title" -> JsString("My Modules"),
      "icon" -> JsString("mortar - board"),
      "items" -> JsArray(Seq(
        JsObject(Seq(
//          "key" -> JsNumber(4),
          "key" -> JsString(UUID.randomUUID().toString),
          "href" -> JsString("http://warwick.ac.uk/cs118"),
          "text" -> JsString("CS118 Programming for Computer Scientists")
        )),
        JsObject(Seq(
//          "key" -> JsNumber(5),
          "key" -> JsString(UUID.randomUUID().toString),
          "href" -> JsString("http://warwick.ac.uk/cs256"),
          "text" -> JsString("CS256 Functional Programming")
        )),
        JsObject(Seq(
          "key" -> JsString(UUID.randomUUID().toString),
          "href" -> JsString("http://warwick.ac.uk/cs324"),
          "text" -> JsString("CS324 Computer Graphics")
        ))
      ))
    ))
  ))

  def requestTileData = Action { request =>
    Ok(Json.toJson(
      Map("type" -> "tiles",
        "tiles" -> Json.stringify(tileData)
      )
    ))
  }
}
