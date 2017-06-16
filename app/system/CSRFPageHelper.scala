package system

import javax.inject.Inject

import play.api.mvc.Request
import play.filters.csrf.{CSRFConfig, CSRFFilter}
import play.twirl.api.{Html, HtmlFormat}

class CSRFPageHelper @Inject() (filter: CSRFFilter, csrfConfig: CSRFConfig) {

  def metaElementToken(implicit request: Request[_]): Html = {
    val token = play.filters.csrf.CSRF.getToken.getOrElse(sys.error("No CSRF token present!"))
    Html(s"""<meta name="_csrf" content="${HtmlFormat.escape(token.value)}"/>""")
  }

  def metaElementHeader(implicit request: Request[_]): Html = {
    Html(s"""<meta name="_csrf_header" content="${csrfConfig.headerName}"/>""")
  }

}
