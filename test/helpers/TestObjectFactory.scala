package helpers

import models.{ActivityPrototype, ActivityRecipients}
import org.joda.time.DateTime
import services.{Group, GroupName}
import warwick.sso.{Department, Name, User, Usercode}

object TestObjectFactory {

  def makeActivityPrototype(): ActivityPrototype =
    ActivityPrototype(
      providerId = "tabula",
      `type` = "due",
      title = "Coursework due",
      text = "Your coursework is due in 7 days",
      tags = Seq.empty,
      replace = Map.empty,
      generatedAt = None,
      shouldNotify = true,
      recipients = ActivityRecipients(
        users = None,
        groups = None
      )
    )

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
    User(
      usercode = Usercode(usercode),
      universityId = None,
      name = Name(None, None),
      email = None,
      department = None,
      isStaffOrPGR = false,
      isStaffNotPGR = false,
      isStudent = true,
      isAlumni = false,
      isFound = true,
      isVerified = true,
      isLoginDisabled = false,
      rawProperties = Map.empty
    )

  def makeNotFoundUser(usercode: String = "user"): User =
    User(
      usercode = Usercode(usercode),
      universityId = None,
      name = Name(None, None),
      email = None,
      department = None,
      isStaffOrPGR = false,
      isStaffNotPGR = false,
      isStudent = true,
      isAlumni = false,
      isFound = false,
      isVerified = true,
      isLoginDisabled = false,
      rawProperties = Map.empty
    )

}
