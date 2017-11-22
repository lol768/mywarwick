package controllers

import info.BuildInfo
import play.api.mvc._

class VersionController extends BaseController {

  def revision = Action(Ok(BuildInfo.gitRevision.take(6)))

}
