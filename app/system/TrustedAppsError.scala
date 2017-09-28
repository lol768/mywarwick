package system

import play.api.libs.ws.WSResponse
import uk.ac.warwick.sso.client.trusted.TrustedApplication._

class TrustedAppsError(code: String, message: Option[String]) extends RuntimeException(message.getOrElse(code))

object TrustedAppsError {
  def fromWSResponse(res: WSResponse): Option[TrustedAppsError] =
    res.header(HEADER_ERROR_CODE)
      .map(new TrustedAppsError(_, res.header(HEADER_ERROR_MESSAGE)))
}