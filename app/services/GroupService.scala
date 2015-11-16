package services

import com.google.inject.{ImplementedBy, Inject}
import org.joda.time.DateTime
import play.api.Configuration
import uk.ac.warwick.userlookup.webgroups.{GroupNotFoundException, GroupServiceException, WarwickGroupsService}
import warwick.sso.{Department, Usercode}

import scala.collection.JavaConversions._
import scala.util.{Failure, Success, Try}

case class GroupName(string: String)

case class Group(
  name: GroupName,
  title: Option[String],
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
      Option(g.getTitle),
      g.getUserCodes.map(Usercode),
      g.getOwners.map(Usercode),
      g.getType,
      Department(None, Some(g.getDepartment), Some(g.getDepartmentCode)),
      new DateTime(g.getLastUpdatedDate)
    )

}

@ImplementedBy(classOf[GroupServiceImpl])
trait GroupService {

  def getWebGroup(groupName: GroupName): Try[Option[Group]]

}

@ImplementedBy(classOf[UnderlyingGroupServiceFactoryImpl])
trait UnderlyingGroupServiceFactory {
  def groupService: uk.ac.warwick.userlookup.GroupService
}

class UnderlyingGroupServiceFactoryImpl @Inject()(
  configuration: Configuration
) extends UnderlyingGroupServiceFactory {
  def groupService = new WarwickGroupsService(configuration.getString("groupservice.webgroups.location").orNull)
}

class GroupServiceImpl @Inject()(
  underlyingGroupServiceFactory: UnderlyingGroupServiceFactory
) extends GroupService {

  val underlyingGroupService = underlyingGroupServiceFactory.groupService

  override def getWebGroup(groupName: GroupName): Try[Option[Group]] =
    try {
      val group = Group(underlyingGroupService.getGroupByName(groupName.string))

      Success(Some(group))
    } catch {
      case _: GroupNotFoundException => Success(None)
      case e: GroupServiceException => Failure(e)
    }

}
