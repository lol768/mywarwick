package services

import models.Permission.Role
import models.Publisher
import system.Logging
import warwick.sso.Usercode

import scala.concurrent.Future

// FIXME trait
class PublisherService extends Logging {
  def allPublishers: Future[Seq[Publisher]] = Nil

  // FIXME implement
  def hasRole(usercode: Usercode, role: Role): Seq[Publisher] = {
    val result = true
    logger.debug(s"hasRole($usercode, $role) = $result")
    result
  }
}
