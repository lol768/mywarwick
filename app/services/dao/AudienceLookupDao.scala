package services.dao

import javax.inject.{Inject, Named}

import org.apache.http.client.methods.HttpGet
import play.api.Configuration
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.libs.ws._
import services.dao.TabulaResponseParsers._
import system.Logging
import uk.ac.warwick.sso.client.trusted.{TrustedApplicationUtils, TrustedApplicationsManager}
import warwick.sso.{UniversityID, User, UserLookupService, Usercode}
import scala.collection.JavaConverters._

import scala.concurrent.Future

case class LookupModule(
  code: String,
  name: String,
  departmentName: String
)

case class LookupSeminarGroup(
  id: String,
  name: String,
  groupSetName: String,
  moduleCode: String
)

case class LookupRelationshipType(
  id: String,
  agentRole: String,
  studentRole: String
)

trait AudienceLookupDao {

  def resolveDepartment(departmentCode: String): Future[Seq[Usercode]]
  def resolveTeachingStaff(departmentCode: String): Future[Seq[Usercode]]
  def resolveAdminStaff(departmentCode: String): Future[Seq[Usercode]]
  def resolveUndergraduates(departmentCode: String): Future[Seq[Usercode]]
  def resolveTaughtPostgraduates(departmentCode: String): Future[Seq[Usercode]]
  def resolveResearchPostgraduates(departmentCode: String): Future[Seq[Usercode]]
  def resolveModule(moduleCode: String): Future[Seq[Usercode]]
  def resolveSeminarGroup(groupId: String): Future[Seq[Usercode]]
  def resolveRelationship(agentId: UniversityID, relationshipType: String): Future[Seq[Usercode]]

  def findModules(query: String): Future[Seq[LookupModule]]
  def findSeminarGroups(query: String): Future[Seq[LookupSeminarGroup]]
  def findRelationships(agentId: UniversityID): Future[Map[LookupRelationshipType, Seq[User]]]

}

@Named("tabula")
class TabulaAudienceLookupDao @Inject()(
  ws: WSClient,
  trustedApplicationsManager: TrustedApplicationsManager,
  configuration: Configuration,
  userLookupService: UserLookupService
) extends AudienceLookupDao with Logging {

  import system.ThreadPools.externalData

  private val tabulaUsercode = configuration.getString("mywarwick.tabula.user")
    .getOrElse(throw new IllegalStateException("Search root configuration missing - check mywarwick.tabula.user in application.conf"))

  private val tabulaModuleQueryUrl = configuration.getString("mywarwick.tabula.moduleQuery")
    .getOrElse(throw new IllegalStateException("Search root configuration missing - check mywarwick.tabula.moduleQuery in application.conf"))

  private val tabulaSmallGroupsQueryUrl = configuration.getString("mywarwick.tabula.groupsQuery")
    .getOrElse(throw new IllegalStateException("Search root configuration missing - check mywarwick.tabula.groupsQuery in application.conf"))

  private val tabulaMemberBaseUrl = configuration.getString("mywarwick.tabula.member.base")
    .getOrElse(throw new IllegalStateException("Search root configuration missing - check mywarwick.tabula.member.base in application.conf"))

  private val tabulaMemberRelationshipsSuffix = configuration.getString("mywarwick.tabula.member.relationshipsSuffix")
    .getOrElse(throw new IllegalStateException("Search root configuration missing - check mywarwick.tabula.member.relationshipsSuffix in application.conf"))

  private def tabulaMemberRelationshipsUrl(member: UniversityID) = s"$tabulaMemberBaseUrl/${member.string}$tabulaMemberRelationshipsSuffix"

  override def resolveDepartment(departmentCode: String): Future[Seq[Usercode]] = ???

  override def resolveTeachingStaff(departmentCode: String): Future[Seq[Usercode]] = ???

  override def resolveAdminStaff(departmentCode: String): Future[Seq[Usercode]] = ???

  override def resolveUndergraduates(departmentCode: String): Future[Seq[Usercode]] = ???

  override def resolveTaughtPostgraduates(departmentCode: String): Future[Seq[Usercode]] = ???

  override def resolveResearchPostgraduates(departmentCode: String): Future[Seq[Usercode]] = ???

  override def resolveModule(moduleCode: String): Future[Seq[Usercode]] = ???

  override def resolveSeminarGroup(groupId: String): Future[Seq[Usercode]] = ???

  def resolveRelationship(agentId: UniversityID, relationshipType: String): Future[Seq[Usercode]] = ???

  override def findModules(query: String): Future[Seq[LookupModule]] = {
    getAsJson(tabulaModuleQueryUrl, Seq("query" -> query))
      .map(_.validate[Seq[LookupModule]].fold(
        invalid => {
          logger.error(s"Could not parse JSON result from Tabula: $invalid")
          Seq()
        },
        modules => modules
      ))
  }

  override def findSeminarGroups(query: String): Future[Seq[LookupSeminarGroup]] = {
    getAsJson(tabulaSmallGroupsQueryUrl, Seq("query" -> query))
      .map(_.validate[Seq[LookupSeminarGroup]].fold(
        invalid => {
          logger.error(s"Could not parse JSON result from Tabula: $invalid")
          Seq()
        },
        groups => groups
      ))
  }

  override def findRelationships(agentId: UniversityID): Future[Map[LookupRelationshipType, Seq[User]]] = {
    getAuthenticatedAsJson(tabulaMemberRelationshipsUrl(agentId))
      .map(_.validate[TabulaResponseParsers.MemberRelationship.MemberRelationshipsResponse](TabulaResponseParsers.MemberRelationship.reads).fold(
        invalid => {
          logger.error(s"Could not parse JSON result from Tabula: $invalid")
          Map.empty
        },
        response => {
          val relationships = response.relationships.groupBy(_.relationshipType).mapValues(_.head.students)
          val allUsercodes = relationships.values.flatMap(users => users.map(u => Usercode(u.userId))).toSeq
          val allUsers = userLookupService.getUsers(allUsercodes).toOption
          relationships.mapValues(users => users.flatMap(u => allUsers.flatMap(_.get(Usercode(u.userId)))))
        }
      ))
  }


  private def getAsJson(url: String, params: Seq[(String, String)]): Future[JsValue] = {
    ws.url(url).withQueryString(params:_*).get().map(response => response.json)
  }

  private def getAuthenticatedAsJson(url: String, params: Seq[(String, String)] = Nil): Future[JsValue] = {
    setupRequest(url).withQueryString(params:_*).get().map(response => response.json)
  }

  private def setupRequest(url: String): WSRequest = {
    val trustedHeaders = TrustedApplicationUtils.getRequestHeaders(trustedApplicationsManager.getCurrentApplication, tabulaUsercode, url)
      .asScala.map(h => h.getName -> h.getValue).toSeq

    ws
      .url(url)
      .withHeaders(trustedHeaders: _*)
  }
}

object TabulaResponseParsers {
  implicit val lookupModuleReads: Reads[LookupModule] = (
    (__ \ "code").read[String] and
      (__ \ "name").read[String] and
      (__ \ "department").read[String]
    )(LookupModule.apply _)

  implicit val lookupSeminarGroupReads: Reads[LookupSeminarGroup] = (
    (__ \ "id").read[String] and
      (__ \ "name").read[String] and
      (__ \ "groupSet" \ "name").read[String] and
      (__ \ "module" \ "code").read[String]
    )(LookupSeminarGroup.apply _)

  case class TabulaUserData(userId: String, universityId: String)
  private implicit val tabulaUserDataReads = Json.reads[TabulaUserData]

  object MemberRelationship {
    case class MemberRelationship(relationshipType: LookupRelationshipType, students: Seq[TabulaUserData])
    case class MemberRelationshipsResponse(
      relationships: Seq[MemberRelationship]
    )
    private implicit val lookupRelationshipTypeReads = Json.reads[LookupRelationshipType]
    private implicit val memberRelationshipReads = (
      (__ \ "relationshipType").read[LookupRelationshipType] and
        (__ \ "students").read[Seq[TabulaUserData]]
    )(MemberRelationship.apply _)
    val reads: Reads[MemberRelationshipsResponse] = (__ \ "relationships").read[Seq[MemberRelationship]].map(MemberRelationshipsResponse.apply)
  }
}
