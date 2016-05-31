package models.news

import warwick.sso.GroupName

case class Audience(components: Seq[Audience.Component] = Nil, public: Boolean = false) {
  if (public && components.nonEmpty) {
    throw new IllegalArgumentException("Public audience can't have components")
  }
}

object Audience {
  val Public = Audience(public = true)

  // Pieces of audience
  sealed trait Component
  // Pieces of department
  trait DepartmentSubset
  case class WebgroupAudience(groupName: GroupName) extends Component
  case class ModuleAudience(moduleCode: String) extends Component
  case class DepartmentAudience(deptCode: String, subset: Seq[DepartmentSubset]) extends Component
  //case object AdminStaff extends Component
  //case object UndergradStudentsFirstYear extends Component
  //case object UndergradStudentsFinalYear extends Component
  case object All extends DepartmentSubset
  case object Staff extends Component with DepartmentSubset
  case object TeachingStaff extends Component with DepartmentSubset
  case object UndergradStudents extends Component with DepartmentSubset
  case object TaughtPostgrads extends Component with DepartmentSubset
  case object ResearchPostgrads extends Component with DepartmentSubset

  object ComponentParameter {
    def unapply(paramValue: String): Option[Component] = paramValue match {
      case "Staff" => Some(Staff)
      case "TeachingStaff" => Some(TeachingStaff)
      case "UndergradStudents" => Some(UndergradStudents)
      case "TaughtPostgrads" => Some(TaughtPostgrads)
      case "ResearchPostgrads" => Some(ResearchPostgrads)
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
}