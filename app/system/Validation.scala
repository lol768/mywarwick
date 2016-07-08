package system

import java.net.URL

import scala.util.Try

object Validation {

  private val VALID_URL_PROTOCOLS = Seq("http", "https")

  val url = (optionalUrl: Option[String]) =>
    optionalUrl.isEmpty || optionalUrl
      .flatMap(s => Try(new URL(s)).toOption)
      .exists(u => VALID_URL_PROTOCOLS.contains(u.getProtocol))

}
