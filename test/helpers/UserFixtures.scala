package helpers

import org.joda.time.DateTime
import warwick.sso._

/** Access via Fixtures.user */
private[helpers] object UserFixtures {

  def makeGroup(name: String = "in-elab", members: Seq[String] = Seq("a", "b")): Group =
    Group(
      GroupName(name),
      Some("ITS web team"),
      members.map(Usercode),
      Seq(Usercode("a")),
      "Arbitrary",
      Department(None, Some("Information Technology Services"), Some("in")),
      DateTime.now()
    )

  def makeFoundUser(usercode: String = "user"): User =
    Users.create(Usercode(usercode), student = true)

  def makeNotFoundUser(usercode: String = "user"): User =
    Users.create(Usercode(usercode), found = false)

  def loginContext(u: Option[User]) = new LoginContext {
    override val user: Option[User] = u
    override val actualUser: Option[User] = None
    override def loginUrl(target: Option[String]): String = "https://app.example.com/login"
    override def userHasRole(role: RoleName) = false
    override def actualUserHasRole(role: RoleName) = false
  }
}
