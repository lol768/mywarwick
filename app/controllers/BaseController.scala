package controllers

import play.api.mvc.InjectedController
import system.{ImplicitRequestContext, Logging}

/**
  *    Love controllers?
  *
  * You'll love BaseController
  */
abstract class BaseController
  extends InjectedController
  with Logging
  with ImplicitRequestContext
