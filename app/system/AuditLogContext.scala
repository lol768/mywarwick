package system

import warwick.sso.User

/**
  * Holds just the User properties. Should be cheap to create, from an object that
  * already has these objects, so that it can be passed to the audit logger.
  */
case class AuditLogContext(
  user: Option[User],
  actualUser: Option[User]
)
