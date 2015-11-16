package services

import com.google.inject.{ImplementedBy, Inject}
import warwick.sso.{UserLookupService, Usercode}

@ImplementedBy(classOf[ActivityTargetServiceImpl])
trait ActivityTargetService {

  def getRecipients(usercodes: Seq[Usercode], groupNames: Seq[GroupName]): Set[Usercode]

}

class ActivityTargetServiceImpl @Inject()(
  userLookupService: UserLookupService,
  groupService: GroupService
) extends ActivityTargetService {

  override def getRecipients(usercodes: Seq[Usercode], groupNames: Seq[GroupName]): Set[Usercode] = {
    val usercodeRecipients = filterValidUsercodes(usercodes)
    val groupRecipients = getGroupMembers(groupNames)

    Set.empty ++ usercodeRecipients ++ groupRecipients
  }

  private def filterValidUsercodes(usercodes: Seq[Usercode]): Iterable[Usercode] =
    userLookupService
      .getUsers(usercodes)
      .getOrElse(Map.empty)
      .filter { case (_, user) => user.isFound } // workaround for SSO-1675
      .map { case (usercode, _) => usercode }

  private def getGroupMembers(groupNames: Seq[GroupName]): Seq[Usercode] =
    groupNames
      .flatMap(groupService.getWebGroup(_).getOrElse(None))
      .flatMap(_.members)

}
