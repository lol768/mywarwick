package helpers

import play.api.libs.json.JsValue

/**
  * Typed wrapper for the response from /user/info.
  */
class UserInfoResponse(js: JsValue) {
  def usercode: Option[String] = (js \ "user" \ "usercode").asOpt[String]
  def loginUrl: String = (js \ "links" \ "login").as[String]
  def logoutUrl: String = (js \ "links" \ "logout").as[String]
}
