package controllers

import info.BuildInfo

class VersionController extends MyController {

  def revision = Action(Ok(BuildInfo.gitRevision.take(6)))

}
