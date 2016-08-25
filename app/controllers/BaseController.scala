package controllers

import play.api.mvc.Controller
import system.{ImplicitRequestContext, Logging}


abstract class BaseController extends Controller with Logging with ImplicitRequestContext
