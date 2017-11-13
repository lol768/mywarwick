package services.dao

import java.io.IOException
import javax.inject.{Inject, Named}

import models.Audience.UndergradStudents
import play.api.Configuration
import play.api.data.validation.ValidationError
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.libs.ws._
import system.{Logging, TrustedAppsError}
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
  def resolveUndergraduates(departmentCode: String, level: UndergradStudents): Future[Seq[Usercode]]
  def resolveTaughtPostgraduates(departmentCode: String): Future[Seq[Usercode]]
  def resolveResearchPostgraduates(departmentCode: String): Future[Seq[Usercode]]
  def resolveModule(moduleCode: String): Future[Seq[Usercode]]
  def resolveSeminarGroup(groupId: String): Future[Seq[Usercode]]
  def resolveRelationship(agentId: UniversityID, relationshipType: String): Future[Seq[Usercode]]

  def getSeminarGroupById(groupId: String): Future[Option[LookupSeminarGroup]]

  def findModules(query: String): Future[Seq[LookupModule]]
  def findSeminarGroups(query: String): Future[Seq[LookupSeminarGroup]]
  def findRelationships(agentId: UniversityID): Future[Map[LookupRelationshipType, Seq[User]]]

}

@Named("tabula")
class TabulaAudienceLookupDao @Inject()(
  ws: WSClient,
  trustedApplicationsManager: TrustedApplicationsManager,
  val configuration: Configuration,
  userLookupService: UserLookupService
) extends AudienceLookupDao with Logging with TabulaAudienceLookupProperties {

  import system.ThreadPools.externalData

  private val tabulaUsercode = configuration.getString("mywarwick.tabula.user")
    .getOrElse(throw new IllegalStateException("Search root configuration missing - check mywarwick.tabula.user in application.conf"))

  private def handleValidationError[A](errors: Seq[(JsPath, Seq[ValidationError])], fallback: A): A = {
    logger.error(s"Could not parse JSON result from Tabula:")
    errors.foreach { case (path, validationErrors) =>
      logger.error(s"$path: ${validationErrors.map(_.message).mkString(", ")}")
    }
    fallback
  }

  private def parseUsercodeSeq(jsValue: JsValue): Seq[Usercode] = {
    TabulaResponseParsers.validateAPIResponse(jsValue, TabulaResponseParsers.usercodesResultReads).fold(
      handleValidationError(_, Seq()),
      usercodes => usercodes
    )
  }

  override def resolveDepartment(departmentCode: String): Future[Seq[Usercode]] = {
    getAuthenticatedAsJson(tabulaDepartmentAllUrl(departmentCode)).map(parseUsercodeSeq)
  }

  override def resolveTeachingStaff(departmentCode: String): Future[Seq[Usercode]] = {
    getAuthenticatedAsJson(tabulaDepartmentTeachingStaffUrl(departmentCode)).map(parseUsercodeSeq)
  }

  override def resolveAdminStaff(departmentCode: String): Future[Seq[Usercode]] = {
    getAuthenticatedAsJson(tabulaDepartmentAdminStaffUrl(departmentCode)).map(parseUsercodeSeq)
  }

  override def resolveUndergraduates(departmentCode: String, level: UndergradStudents): Future[Seq[Usercode]] = {
    getAuthenticatedAsJson(tabulaDepartmentUndergraduatesUrl(departmentCode, level.value.toLowerCase)).map(parseUsercodeSeq)
  }

  override def resolveTaughtPostgraduates(departmentCode: String): Future[Seq[Usercode]] = {
    getAuthenticatedAsJson(tabulaDepartmentPGTUrl(departmentCode)).map(parseUsercodeSeq)
  }

  override def resolveResearchPostgraduates(departmentCode: String): Future[Seq[Usercode]] = {
    getAuthenticatedAsJson(tabulaDepartmentPGRUrl(departmentCode)).map(parseUsercodeSeq)
  }

  override def resolveModule(moduleCode: String): Future[Seq[Usercode]] = {
    getAuthenticatedAsJson(tabulaModuleStudentsUrl(moduleCode)).map(parseUsercodeSeq)
  }

  override def resolveSeminarGroup(groupId: String): Future[Seq[Usercode]] = {
    getAuthenticatedAsJson(tabulaSmallGroupsLookupUrl(groupId)).map(
      TabulaResponseParsers.validateAPIResponse(_, TabulaResponseParsers.smallGroupReads).fold(
        handleValidationError(_, Seq()),
        users => users.map(u => Usercode(u.userId))
      )
    )
  }

  override def resolveRelationship(agentId: UniversityID, relationshipType: String): Future[Seq[Usercode]] = {
    findRelationships(agentId).map(
      _.filterKeys(_.id == relationshipType).toSeq.headOption.map { case (_, users) => users.map(_.usercode) }.getOrElse(Seq())
    )
  }

  override def getSeminarGroupById(groupId: String): Future[Option[LookupSeminarGroup]] = {
    getAuthenticatedAsJson(tabulaSmallGroupsLookupUrl(groupId)).map(
      TabulaResponseParsers.validateAPIResponse(_, TabulaResponseParsers.seminarGroupReads(groupId)).fold(
        handleValidationError(_, None),
        group => Some(group)
      )
    )
  }

  override def findModules(query: String): Future[Seq[LookupModule]] = {
    getAsJson(tabulaModuleQueryUrl, Seq("query" -> query))
      .map(_.validate[Seq[LookupModule]](Reads.seq(TabulaResponseParsers.lookupModuleReads)).fold(
        handleValidationError(_, Seq()),
        modules => modules
      ))
  }

  override def findSeminarGroups(query: String): Future[Seq[LookupSeminarGroup]] = {
    getAsJson(tabulaSmallGroupsQueryUrl, Seq("query" -> query))
      .map(_.validate[Seq[LookupSeminarGroup]](Reads.seq(TabulaResponseParsers.lookupSeminarGroupReads)).fold(
        handleValidationError(_, Seq()),
        groups => groups
      ))
  }

  override def findRelationships(agentId: UniversityID): Future[Map[LookupRelationshipType, Seq[User]]] = {
    getAuthenticatedAsJson(tabulaMemberRelationshipsUrl(agentId))
      .map(_.validate[TabulaResponseParsers.MemberRelationship.MemberRelationshipsResponse](TabulaResponseParsers.MemberRelationship.reads).fold(
        handleValidationError(_, Map.empty),
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
    setupRequest(url).withQueryString(params:_*).get()
      .map(response => {
        TrustedAppsError.fromWSResponse(response).foreach { e =>
          throw e
        }
        if (response.status >= 400) {
          throw new IOException(s"Response code ${response.status} from audience lookup ${url}")
        }
        response.json
      })
  }

  private def setupRequest(url: String): WSRequest = {
    val trustedHeaders = TrustedApplicationUtils.getRequestHeaders(trustedApplicationsManager.getCurrentApplication, tabulaUsercode, url)
      .asScala.map(h => h.getName -> h.getValue).toSeq

    ws
      .url(url)
      .withHeaders(trustedHeaders: _*)
  }
}

trait TabulaAudienceLookupProperties {

  def configuration: Configuration

  private val tabulaDepartmentBaseUrl = configuration.getString("mywarwick.tabula.department.base")
    .getOrElse(throw new IllegalStateException("Configuration missing - check mywarwick.tabula.department.base in application.conf"))

  private val tabulaDepartmentAllSuffix = configuration.getString("mywarwick.tabula.department.allSuffix")
    .getOrElse(throw new IllegalStateException("Configuration missing - check mywarwick.tabula.department.allSuffix in application.conf"))

  protected def tabulaDepartmentAllUrl(departmentCode: String) = s"$tabulaDepartmentBaseUrl/${departmentCode.toLowerCase}$tabulaDepartmentAllSuffix"

  private val tabulaDepartmentTeachingStaffSuffix = configuration.getString("mywarwick.tabula.department.teachingStaffSuffix")
    .getOrElse(throw new IllegalStateException("Configuration missing - check mywarwick.tabula.department.teachingStaffSuffix in application.conf"))

  protected def tabulaDepartmentTeachingStaffUrl(departmentCode: String) = s"$tabulaDepartmentBaseUrl/${departmentCode.toLowerCase}$tabulaDepartmentTeachingStaffSuffix"

  private val tabulaDepartmentAdminStaffSuffix = configuration.getString("mywarwick.tabula.department.adminStaffSuffix")
    .getOrElse(throw new IllegalStateException("Configuration missing - check mywarwick.tabula.department.adminStaffSuffix in application.conf"))

  protected def tabulaDepartmentAdminStaffUrl(departmentCode: String) = s"$tabulaDepartmentBaseUrl/${departmentCode.toLowerCase}$tabulaDepartmentAdminStaffSuffix"

  // TODO: when Tabula API is updated, update url here to pass UG level param
  private val tabulaDepartmentUndergraduatesSuffix = configuration.getString("mywarwick.tabula.department.undergraduatesSuffix")
    .getOrElse(throw new IllegalStateException("Configuration missing - check mywarwick.tabula.department.undergraduatesSuffix in application.conf"))

  protected def tabulaDepartmentUndergraduatesUrl(departmentCode: String, level: String) = s"$tabulaDepartmentBaseUrl/${departmentCode.toLowerCase}$tabulaDepartmentUndergraduatesSuffix"

  private val tabulaDepartmentPGTSuffix = configuration.getString("mywarwick.tabula.department.pgtSuffix")
    .getOrElse(throw new IllegalStateException("Configuration missing - check mywarwick.tabula.department.pgtSuffix in application.conf"))

  protected def tabulaDepartmentPGTUrl(departmentCode: String) = s"$tabulaDepartmentBaseUrl/${departmentCode.toLowerCase}$tabulaDepartmentPGTSuffix"

  private val tabulaDepartmentPGRSuffix = configuration.getString("mywarwick.tabula.department.pgrSuffix")
    .getOrElse(throw new IllegalStateException("Configuration missing - check mywarwick.tabula.department.pgrSuffix in application.conf"))

  protected def tabulaDepartmentPGRUrl(departmentCode: String) = s"$tabulaDepartmentBaseUrl/${departmentCode.toLowerCase}$tabulaDepartmentPGRSuffix"

  private val tabulaModuleBaseUrl = configuration.getString("mywarwick.tabula.module.base")
    .getOrElse(throw new IllegalStateException("Configuration missing - check mywarwick.tabula.module.base in application.conf"))

  private val tabulaModuleStudentsSuffix = configuration.getString("mywarwick.tabula.module.studentsSuffix")
    .getOrElse(throw new IllegalStateException("Configuration missing - check mywarwick.tabula.module.studentsSuffix in application.conf"))

  protected def tabulaModuleStudentsUrl(moduleCode: String): String = s"$tabulaModuleBaseUrl/${moduleCode.toLowerCase}$tabulaModuleStudentsSuffix"

  private val tabulaModuleQuery = configuration.getString("mywarwick.tabula.module.query")
    .getOrElse(throw new IllegalStateException("Configuration missing - check mywarwick.tabula.module.query in application.conf"))

  protected def tabulaModuleQueryUrl: String = tabulaModuleQuery

  private val tabulaSmallGroupsLookup = configuration.getString("mywarwick.tabula.groups.lookup")
    .getOrElse(throw new IllegalStateException("Configuration missing - check mywarwick.tabula.groups.lookup in application.conf"))

  protected def tabulaSmallGroupsLookupUrl(groupId: String): String = s"$tabulaSmallGroupsLookup/$groupId"

  private val tabulaSmallGroupsQuery = configuration.getString("mywarwick.tabula.groups.query")
    .getOrElse(throw new IllegalStateException("Configuration missing - check mywarwick.tabula.groups.query in application.conf"))

  protected def tabulaSmallGroupsQueryUrl: String = tabulaSmallGroupsQuery

  private val tabulaMemberBaseUrl = configuration.getString("mywarwick.tabula.member.base")
    .getOrElse(throw new IllegalStateException("Configuration missing - check mywarwick.tabula.member.base in application.conf"))

  private val tabulaMemberRelationshipsSuffix = configuration.getString("mywarwick.tabula.member.relationshipsSuffix")
    .getOrElse(throw new IllegalStateException("Configuration missing - check mywarwick.tabula.member.relationshipsSuffix in application.conf"))

  protected def tabulaMemberRelationshipsUrl(member: UniversityID) = s"$tabulaMemberBaseUrl/${member.string}$tabulaMemberRelationshipsSuffix"

}

object TabulaResponseParsers {
  val usercodesResultReads: Reads[Seq[Usercode]] = (__ \ "usercodes").read[Seq[String]].map(s => s.map(Usercode))

  val lookupModuleReads: Reads[LookupModule] = (
    (__ \ "code").read[String] and
      (__ \ "name").read[String] and
      (__ \ "department").read[String]
    )(LookupModule.apply _)

  val lookupSeminarGroupReads: Reads[LookupSeminarGroup] = (
    (__ \ "id").read[String] and
      (__ \ "name").read[String] and
      (__ \ "groupSet" \ "name").read[String] and
      (__ \ "module" \ "code").read[String]
    ) (LookupSeminarGroup.apply _)

  def seminarGroupReads(id: String): Reads[LookupSeminarGroup] = (
    Reads.pure(id) and
      (__ \ "group" \"name").read[String] and
      Reads.pure("") and
      Reads.pure("")
    ) (LookupSeminarGroup.apply _)

  case class TabulaUserData(userId: String, universityId: String)
  private val tabulaUserDataReads = Json.reads[TabulaUserData]

  object MemberRelationship {
    case class MemberRelationship(relationshipType: LookupRelationshipType, students: Seq[TabulaUserData])
    case class MemberRelationshipsResponse(
      relationships: Seq[MemberRelationship]
    )
    private val lookupRelationshipTypeReads = Json.reads[LookupRelationshipType]
    private val memberRelationshipReads = (
      (__ \ "relationshipType").read[LookupRelationshipType](lookupRelationshipTypeReads) and
        (__ \ "students").read[Seq[TabulaUserData]](Reads.seq(tabulaUserDataReads))
    )(MemberRelationship.apply _)
    val reads: Reads[MemberRelationshipsResponse] =
      (__ \ "relationships").read[Seq[MemberRelationship]](Reads.seq(memberRelationshipReads))
        .map(MemberRelationshipsResponse.apply)
  }

  val smallGroupReads: Reads[Seq[TabulaUserData]] =
    (__ \ "group" \ "students").read[Seq[TabulaUserData]](Reads.seq(tabulaUserDataReads))

  private case class ErrorMessage(message: String)
  private val errorMessageReads = Json.reads[ErrorMessage]

  def validateAPIResponse[A](jsValue: JsValue, parser: Reads[A]): JsResult[A] = {
    (jsValue \ "success").validate[Boolean].flatMap {
      case false =>
        val status = (jsValue \ "status").validate[String].asOpt
        val errors = (jsValue \ "errors").validate[Seq[ErrorMessage]](Reads.seq(errorMessageReads)).asOpt
        JsError(__ \ "success", "Tabula API response not successful%s%s".format(
          status.map(s => s" (status: $s)").getOrElse(""),
          errors.map(e => s": ${e.map(_.message).mkString(", ")}").getOrElse("")
        ))
      case true =>
        jsValue.validate[A](parser)
    }
  }
}
