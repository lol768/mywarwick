package controllers

import play.api.mvc.Controller
import system.{ImplicitRequestContext, Logging}

/**
  *    Love controllers?
  *
  * You'll love BaseController
  */
abstract class BaseController
  extends Controller
  with Logging
  with ImplicitRequestContext
