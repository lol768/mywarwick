package models.news

case class Audience(components: Seq[Audience.Component] = Nil)

object Audience {
  // Pieces of audience
  sealed trait Component
  // Pieces of department
  trait DepartmentSubset
  case class WebgroupAudience(groupName: String) extends Component
  case class ModuleAudience(moduleCode: String) extends Component
  case class DepartmentAudience(deptCode: String, subset: Seq[DepartmentSubset]) extends Component
  case object TeachingStaff extends Component
  case object AdminStaff extends Component
  case object Staff extends DepartmentSubset
  case object UndergradStudents extends Component with DepartmentSubset
  case object UndergradStudentsFirstYear extends Component
  case object UndergradStudentsFinalYear extends Component
  case object TaughtPostgrads extends Component with DepartmentSubset
  case object ResearchPostgrads extends Component with DepartmentSubset
}