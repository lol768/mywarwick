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

}
