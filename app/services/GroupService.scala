package services

import com.google.inject.{ImplementedBy, Inject}
import org.joda.time.DateTime
import play.api.Configuration
import uk.ac.warwick.userlookup.webgroups.WarwickGroupsService
import warwick.sso.{Department, Usercode}

import scala.collection.JavaConversions._

case class GroupName(string: String)

case class Group(
  name: GroupName,
  title: String,
  members: Seq[Usercode],
  owners: Seq[Usercode],
  `type`: String,
  department: Department,
  lastUpdated: DateTime
)

object Group {

  def apply(g: uk.ac.warwick.userlookup.Group): Group =
    Group(
      GroupName(g.getName),
      g.getTitle,
      g.getUserCodes.map(Usercode),
      g.getOwners.map(Usercode),
      g.getType,
      Department(None, Some(g.getDepartment), Some(g.getDepartmentCode)),
      new DateTime(g.getLastUpdatedDate)
    )

}

@ImplementedBy(classOf[GroupServiceImpl])
trait GroupService {

  def getWebGroup(groupName: GroupName): Option[Group]

}

class GroupServiceImpl @Inject()(
  configuration: Configuration
) extends GroupService {

  private lazy val underlyingGroupService: uk.ac.warwick.userlookup.GroupService =
    new WarwickGroupsService(configuration.getString("groupservice.webgroups.location").orNull)

  override def getWebGroup(groupName: GroupName): Option[Group] =
    Option(underlyingGroupService.getGroupByName(groupName.string))
      .filter(_.isVerified)
      .map(Group.apply)

}
