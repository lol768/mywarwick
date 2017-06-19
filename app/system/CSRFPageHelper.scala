package system

import javax.inject.Inject

import play.api.mvc.RequestHeader
import play.filters.csrf.CSRF.Token
import play.filters.csrf.CSRFConfig
import play.twirl.api.{Html, HtmlFormat}

class CSRFPageHelper @Inject() (csrfConfig: CSRFConfig) {

  var token: Token = _


  def metaElementToken(): Html = {
    Html(s"""<meta name="_csrf" content="${HtmlFormat.escape(token.value)}"/>""")
  }

  def metaElementHeader(): Html = {
    Html(s"""<meta name="_csrf_header" content="${csrfConfig.headerName}"/>""")
  }

  // Provide a convenience method which doesn't require an implicit RequestHeader in
  // each view that we wish to have a CSRF field in.
  def formField(): Html = {
    Html(s"""<input type="hidden" name="${csrfConfig.tokenName}" value="${HtmlFormat.escape(token.value)}">""")
  }

}
