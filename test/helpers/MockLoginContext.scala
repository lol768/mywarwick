package helpers

import warwick.sso.{LoginContext, RoleName, User}

class MockLoginContext(
  val user: Option[User] = None,
  val actualUser: Option[User] = None
) extends LoginContext {

  override def loginUrl(target: Option[String]): String = "https://app.example.com/login"

  override def userHasRole(role: RoleName) = false

  override def actualUserHasRole(role: RoleName) = false

}
