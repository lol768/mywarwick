package system

import javax.inject.Inject

import play.filters.csrf.{CSRF, CSRFConfig}

class CSRFPageHelperFactory @Inject() (csrfConfig: CSRFConfig) {

  def getInstance(token: Option[CSRF.Token]): CSRFPageHelper = {
    new CSRFPageHelper(csrfConfig, token)
  }

}
