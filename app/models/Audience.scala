package models

import enumeratum.EnumEntry
import warwick.sso.{GroupName, UniversityID, Usercode}

import scala.util.matching.Regex

case class Audience(components: Seq[Audience.Component] = Nil) {
  val public: Boolean = components.contains(Audience.PublicAudience)

  if (public && components.length > 1) {
    throw new IllegalArgumentException("Public audience can't have any other components")
  }
}

object Audience {
  val Public = Audience(Seq(PublicAudience))

  def usercodes(usercodes: Seq[Usercode]): Audience = {
    Audience(usercodes match {
      case _ :: _ => Seq(UsercodesAudience(usercodes.toSet))
      case _ => Nil
    })
  }

  def usercode(usercode: Usercode): Audience = {
    usercodes(Seq(usercode))
  }

  def webGroup(groupName: GroupName): Audience = {
    Audience(Seq(WebGroupAudience(groupName)))
  }

  // Pieces of audience
  sealed trait Component extends EnumEntry

  // Pieces of department
  sealed trait DepartmentSubset extends Component {
    val displayName: String = toString
  }

  case class WebGroupAudience(groupName: GroupName) extends Component // No longer available in Audience Picker UI

  case object PublicAudience extends Component

  case class UsercodesAudience(usercodes: Set[Usercode]) extends DepartmentSubset {
    override val displayName: String = s"Usercodes( ${usercodes.map(_.string).mkString(", ")})"

    def size: Int = usercodes.size

    def getLikelyInvalidUsercodes: Set[Usercode] = Audience.helper.getLikelyInvalidUsercodes(this)

    def allUsercodesAreLikelyInvalid: Boolean = Audience.helper.maybeAllInvalidUsercodes(usercodes)

    def allUsercodesAreLikelyValid: Boolean = Audience.helper.maybeAllValidUsercodes(usercodes)

    def getLikelyValidUsercodes: Set[Usercode] = Audience.helper.getLikelyValidUsercodes(this)
  }


  case class ModuleAudience(moduleCode: String) extends DepartmentSubset

  case class SeminarGroupAudience(groupId: String) extends DepartmentSubset

  case class RelationshipAudience(relationshipType: String, agentId: UniversityID) extends DepartmentSubset

  case class DepartmentAudience(deptCode: String, subset: Seq[DepartmentSubset]) extends Component

  case class ResidenceAudience(residence: Residence) extends Component {
    val displayName: String = "Halls of residence"
  }

  abstract class Residence(val id: String, val displayName: String, val queryParameters: Seq[(String, String)])

  object Residence {

    case object ArthurVick extends Residence("arthurVick", "Arthur Vick", Seq(("hallsOfResidence", "Arthur+Vick")))
    case object Benefactors extends Residence("benefactors", "Benefactors", Seq(("hallsOfResidence", "Benefactors")))
    case object Bluebell extends Residence("bluebell", "Bluebell", Seq(("hallsOfResidence", "Bluebell")))
    case object Claycroft extends Residence("claycroft", "Claycroft", Seq(("hallsOfResidence", "Claycroft")))
    case object Cryfield extends Residence("cryfield", "Cryfield", Seq(("hallsOfResidence", "Cryfield")))
    case object Heronbank extends Residence("heronbank", "Heronbank", Seq(("hallsOfResidence", "Heronbank")))
    case object JackMartin extends Residence("jackMartin", "Jack Martin", Seq(("hallsOfResidence", "Jack+Martin")))
    case object Lakeside extends Residence("lakeside", "Lakeside", Seq(("hallsOfResidence", "Lakeside")))
    case object Redfern extends Residence("redfern", "Redfern", Seq(("hallsOfResidence", "Redfern")))
    case object Rootes extends Residence("rootes", "Rootes", Seq(("hallsOfResidence", "Rootes")))
    case object Sherbourne extends Residence("sherbourne", "Sherbourne", Seq(("hallsOfResidence", "Sherbourne")))
    case object Tocil extends Residence("tocil", "Tocil", Seq(("hallsOfResidence", "Tocil")))
    case object Westwood extends Residence("westwood", "Westwood", Seq(("hallsOfResidence", "Westwood")))
    case object Whitefields extends Residence("whitefields", "Whitefields", Seq(("hallsOfResidence", "Whitefields")))
    case object All extends Residence("all", "All", all.flatMap(_.queryParameters))

    def all: Seq[Residence] = Seq(
      ArthurVick, Benefactors, Bluebell, Claycroft, Cryfield, Heronbank, JackMartin,
      Lakeside, Redfern, Rootes, Sherbourne, Tocil, Westwood, Whitefields
    )

    def fromId(id: String): Residence = {
      if (id == All.id) {
        All
      } else {
        all.find(_.id == id)
          .getOrElse(throw new IllegalArgumentException(s"Cannot find Residence AudienceComponent with id $id"))
      }
    }
  }

  case object All extends DepartmentSubset

  case object Staff extends DepartmentSubset {
    override val displayName: String = "Staff"
  }

  //  "these are kept only for existing data, we no longer differentiate between admin or teaching staff.",
  //  "NEWSTART-1313"
  case object TeachingStaff extends DepartmentSubset {
    override val displayName = "Teaching Staff"
  }
  case object AdminStaff extends DepartmentSubset {
    override val displayName = "Administrative Staff"
  }

  sealed abstract class UndergradStudents(val value: String) extends DepartmentSubset

  object UndergradStudents {
    case object All extends UndergradStudents("All")
    case object First extends UndergradStudents("First")
    case object Second extends UndergradStudents("Second")
    case object Final extends UndergradStudents("Final")
    def values = Seq(All, First, Second, Final)
    def fromValue(value: String): Option[UndergradStudents] = values.find(_.value == value)
    def levelToNumStr(level: UndergradStudents): String = level match {
      case First => "1"
      case Second => "2"
      case Final => "f"
      case _ => ""
    }
  }

  case object TaughtPostgrads extends DepartmentSubset {
    override val displayName = "Taught Postgraduates"
  }

  case object ResearchPostgrads extends DepartmentSubset {
    override val displayName = "Research Postgraduates"
  }

  sealed abstract class OptIn(val optInType: String, val optInValue: String, val description: String) extends Component

  sealed abstract class LocationOptIn(val value: String, override val description: String) extends OptIn(LocationOptIn.optInType, value, description)

  object LocationOptIn {
    val optInType = "Location"

    case object CentralCampusResidences extends LocationOptIn("CentralCampusResidences", "Central campus residences")

    case object WestwoodResidences extends LocationOptIn("WestwoodResidences", "Westwood residences")

    case object Coventry extends LocationOptIn("Coventry", "Coventry")

    case object Kenilworth extends LocationOptIn("Kenilworth", "Kenilworth")

    case object LeamingtonSpa extends LocationOptIn("LeamingtonSpa", "Leamington Spa")

    def values = Seq(CentralCampusResidences, WestwoodResidences, Coventry, Kenilworth, LeamingtonSpa)

    def fromValue(value: String): Option[OptIn] = values.find(_.value == value)
  }

  val moduleCodeRegex: Regex = "^Module:(.+)".r
  val undergradRegex: Regex = "^UndergradStudents:(.+)".r
  val seminarGroupRegex: Regex = "^SeminarGroup:(.+)".r
  val relationshipRegex: Regex = "^Relationship:(.+):(.+)".r
  val webGroupRegex: Regex = "^WebGroup:(.+)".r
  val optInRegex: Regex = "^OptIn:(.+):(.+)".r
  val hallsOfResidenceRegex: Regex = "^hallsOfResidence:(.+)".r

  object ComponentParameter {
    def unapply(paramValue: String): Option[Component] = paramValue match {
      case "Staff" => Some(Staff)
      case "TeachingStaff" => Some(TeachingStaff)
      case "AdminStaff" => Some(AdminStaff)
      case "TaughtPostgrads" => Some(TaughtPostgrads)
      case "ResearchPostgrads" => Some(ResearchPostgrads)
      case undergradRegex(group) => UndergradStudents.fromValue(group)
      case webGroupRegex(webGroup) => Some(WebGroupAudience(GroupName(webGroup)))
      case moduleCodeRegex(code) => Some(ModuleAudience(code))
      case seminarGroupRegex(groupId) => Some(SeminarGroupAudience(groupId))
      case relationshipRegex(relationshipType, agentId) => Some(RelationshipAudience(relationshipType, UniversityID(agentId)))
      case optInRegex(optInType, optInValue) if optInType == LocationOptIn.optInType => LocationOptIn.fromValue(optInValue)
      case hallsOfResidenceRegex(id) =>
        val hall = id.split(":").toList.last
        if (hall == "all") {
          Some(ResidenceAudience(Residence.All))
        } else {
          Some(ResidenceAudience(Residence.fromId(hall)))
        }
      case string if string.nonEmpty => {
        val validUsercodes: Set[Usercode] = string.split("\n").map(_.trim).flatMap { usercode =>
          if (!usercode.contains(":")) Some(Usercode(usercode))
          else None
        }.toSet
        if (validUsercodes.nonEmpty)
          Some(UsercodesAudience(validUsercodes))
        else
          None
      }
      case _ => None
    }
  }

  object DepartmentSubset {
    def unapply(paramValue: String): Option[DepartmentSubset] = paramValue match {
      case "All" => Some(All)
      case ComponentParameter(subset: DepartmentSubset) => Some(subset)
      case _ => None
    }
  }


  object helper {
    val validUsercodePattern: Regex = """^([a-zA-Z0-9\_\-\@\.]+)\Z""".r

    def maybeValidUsercode(usercode: Usercode): Boolean = validUsercodePattern.findAllMatchIn(usercode.string).nonEmpty

    def maybeInvalidUsercode(usercode: Usercode): Boolean = !maybeValidUsercode(usercode)

    def maybeAllValidUsercodes(usercodes: Set[Usercode]): Boolean = usercodes.forall(maybeValidUsercode)

    def maybeAllInvalidUsercodes(usercodes: Set[Usercode]): Boolean = {
      if (usercodes.isEmpty) {
        return false
      }
      usercodes.forall(maybeInvalidUsercode)
    }

    def getLikelyValidUsercodes(usercodesAudiences: UsercodesAudience): Set[Usercode] = {
      usercodesAudiences.usercodes.filter(maybeValidUsercode)
    }

    def getLikelyInvalidUsercodes(usercodesAudiences: UsercodesAudience): Set[Usercode] = {
      usercodesAudiences.usercodes.filter(maybeInvalidUsercode)
    }
  }

}
