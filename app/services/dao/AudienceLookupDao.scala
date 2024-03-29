package services.dao

import java.io.IOException

import javax.inject.{Inject, Named}
import models.Audience.{Residence, UndergradStudents}
import play.api.Configuration
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.libs.ws._
import services.helper.WSRequestUriBuilder
import system.{Logging, TrustedAppsError}
import uk.ac.warwick.sso.client.trusted.{TrustedApplicationUtils, TrustedApplicationsManager}
import utils.UserLookupUtils._
import warwick.sso._

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future}

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
  def resolveStaff(departmentCode: String): Future[Seq[Usercode]]
  def resolveUndergraduatesInDept(departmentCode: String, level: UndergradStudents): Future[Seq[Usercode]]
  def resolveUndergraduatesUniWide(level: UndergradStudents): Future[Seq[Usercode]]
  def resolveTaughtPostgraduates(departmentCode: String): Future[Seq[Usercode]]
  def resolveResearchPostgraduates(departmentCode: String): Future[Seq[Usercode]]
  def resolveModule(moduleCode: String): Future[Seq[Usercode]]
  def resolveSeminarGroup(groupId: String): Future[Seq[Usercode]]
  def resolveRelationship(agentId: UniversityID, relationshipType: String): Future[Seq[Usercode]]
  def resolveResidence(residence: Residence): Future[Seq[Usercode]]

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
)(implicit @Named("externalData") ec: ExecutionContext) extends AudienceLookupDao with Logging with TabulaAudienceLookupProperties {

  private val tabulaUsercode = configuration.getOptional[String]("mywarwick.tabula.user")
    .getOrElse(throw new IllegalStateException("Search root configuration missing - check mywarwick.tabula.user in application.conf"))

  private def handleValidationError[A](errors: Seq[(JsPath, Seq[JsonValidationError])], fallback: A): A = {
    logger.error(s"Could not parse JSON result from Tabula:")
    errors.foreach { case (path, validationErrors) =>
      logger.error(s"$path: ${validationErrors.map(_.message).mkString(", ")}")
    }
    fallback
  }

  private def parseUsercodeSeq(jsValue: JsValue): Seq[Usercode] = {
    TabulaResponseParsers.validateAPIResponse(jsValue, TabulaResponseParsers.usercodesResultReads).fold(
      err => handleValidationError(err, Nil),
      usercodes => usercodes
    )
  }

  override def resolveDepartment(departmentCode: String): Future[Seq[Usercode]] = {
    getAuthenticatedAsJson(tabulaDepartmentAllUrl(departmentCode)).map(parseUsercodeSeq)
  }

  override def resolveStaff(departmentCode: String): Future[Seq[Usercode]] = {
    getAuthenticatedAsJson(tabulaDepartmentStaffUrl(departmentCode)).map(parseUsercodeSeq)
  }

  override def resolveUndergraduatesInDept(departmentCode: String, level: UndergradStudents): Future[Seq[Usercode]] = {
    getAuthenticatedAsJson(tabulaDepartmentUndergraduatesUrl(departmentCode, UndergradStudents.levelToNumStr(level))).map(parseUsercodeSeq)
  }

  override def resolveUndergraduatesUniWide(level: UndergradStudents): Future[Seq[Usercode]] = {
    getAuthenticatedAsJson(tabulaUniWideUndergraduatesUrl(UndergradStudents.levelToNumStr(level))).map(parseUsercodeSeq)
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


  override def resolveResidence(residence: Residence): Future[Seq[Usercode]] = {
    getAuthenticatedAsJson(tabulaAudienceLookUpUrl, residence.queryParameters)
      .map(
        TabulaResponseParsers.validateAPIResponse(_, TabulaResponseParsers.usercodesResultReads).fold(
          handleValidationError(_, Seq()),
          userCodes => userCodes
        )
      )
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
    ws.url(url).addQueryStringParameters(params:_*).get().map(response => response.json)
  }

  private def getAuthenticatedAsJson(url: String, params: Seq[(String, String)] = Nil): Future[JsValue] = {
    setupRequest(url, params).get()
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

  private def setupRequest(url: String, params: Seq[(String, String)] = Nil): WSRequest = {
    val request = ws.url(url).addQueryStringParameters(params: _*)
    val trustedHeaders = TrustedApplicationUtils.getRequestHeaders(
      trustedApplicationsManager.getCurrentApplication,
      tabulaUsercode,
      WSRequestUriBuilder.buildUri(request).toString
    )
      .asScala.map(h => h.getName -> h.getValue).toSeq
    request.addHttpHeaders(trustedHeaders: _*)
  }

}

trait TabulaAudienceLookupProperties {

  def configuration: Configuration

  private val tabulaAPIBaseUrl = configuration.getOptional[String]("mywarwick.tabula.apiBase")
    .getOrElse(throw new IllegalStateException("Configuration missing - check mywarwick.tabula.apiBase in application.conf"))

  private val tabulaDepartmentBaseUrl = configuration.getOptional[String]("mywarwick.tabula.department.base")
    .getOrElse(throw new IllegalStateException("Configuration missing - check mywarwick.tabula.department.base in application.conf"))

  private val tabulaDepartmentAllSuffix = configuration.getOptional[String]("mywarwick.tabula.department.allSuffix")
    .getOrElse(throw new IllegalStateException("Configuration missing - check mywarwick.tabula.department.allSuffix in application.conf"))

  protected def tabulaDepartmentAllUrl(departmentCode: String) = s"$tabulaDepartmentBaseUrl/${departmentCode.toLowerCase}$tabulaDepartmentAllSuffix"

  private val tabulaDepartmentStaffSuffix = configuration.getOptional[String]("mywarwick.tabula.department.staff")
    .getOrElse(throw new IllegalStateException("Configuration missing - check mywarwick.tabula.department.staff in application.conf"))

  protected def tabulaDepartmentStaffUrl(departmentCode: String) = s"$tabulaDepartmentBaseUrl/${departmentCode.toLowerCase}$tabulaDepartmentStaffSuffix"

  private val tabulaUndergraduatesSuffix = configuration.getOptional[String]("mywarwick.tabula.undergraduatesSuffix")
    .getOrElse(throw new IllegalStateException("Configuration missing - check mywarwick.tabula.undergraduatesSuffix in application.conf"))

  protected def tabulaDepartmentUndergraduatesUrl(departmentCode: String, level: String) =
    s"$tabulaDepartmentBaseUrl/${departmentCode.toLowerCase}$tabulaUndergraduatesSuffix${if (level.nonEmpty) s"?level=$level" else ""}"

  protected def tabulaUniWideUndergraduatesUrl(level: String) =
    s"$tabulaAPIBaseUrl/$tabulaUndergraduatesSuffix${if (level.nonEmpty) s"?level=$level" else ""}"

  private val tabulaDepartmentPGTSuffix = configuration.getOptional[String]("mywarwick.tabula.department.pgtSuffix")
    .getOrElse(throw new IllegalStateException("Configuration missing - check mywarwick.tabula.department.pgtSuffix in application.conf"))

  protected def tabulaDepartmentPGTUrl(departmentCode: String) = s"$tabulaDepartmentBaseUrl/${departmentCode.toLowerCase}$tabulaDepartmentPGTSuffix"

  private val tabulaDepartmentPGRSuffix = configuration.getOptional[String]("mywarwick.tabula.department.pgrSuffix")
    .getOrElse(throw new IllegalStateException("Configuration missing - check mywarwick.tabula.department.pgrSuffix in application.conf"))

  protected def tabulaDepartmentPGRUrl(departmentCode: String) = s"$tabulaDepartmentBaseUrl/${departmentCode.toLowerCase}$tabulaDepartmentPGRSuffix"

  private val tabulaModuleBaseUrl = configuration.getOptional[String]("mywarwick.tabula.module.base")
    .getOrElse(throw new IllegalStateException("Configuration missing - check mywarwick.tabula.module.base in application.conf"))

  private val tabulaModuleStudentsSuffix = configuration.getOptional[String]("mywarwick.tabula.module.studentsSuffix")
    .getOrElse(throw new IllegalStateException("Configuration missing - check mywarwick.tabula.module.studentsSuffix in application.conf"))

  protected def tabulaModuleStudentsUrl(moduleCode: String): String = s"$tabulaModuleBaseUrl/${moduleCode.toLowerCase}$tabulaModuleStudentsSuffix"

  private val tabulaModuleQuery = configuration.getOptional[String]("mywarwick.tabula.module.query")
    .getOrElse(throw new IllegalStateException("Configuration missing - check mywarwick.tabula.module.query in application.conf"))

  protected def tabulaModuleQueryUrl: String = tabulaModuleQuery

  private val tabulaSmallGroupsLookup = configuration.getOptional[String]("mywarwick.tabula.groups.lookup")
    .getOrElse(throw new IllegalStateException("Configuration missing - check mywarwick.tabula.groups.lookup in application.conf"))

  protected def tabulaSmallGroupsLookupUrl(groupId: String): String = s"$tabulaSmallGroupsLookup/$groupId"

  private val tabulaSmallGroupsQuery = configuration.getOptional[String]("mywarwick.tabula.groups.query")
    .getOrElse(throw new IllegalStateException("Configuration missing - check mywarwick.tabula.groups.query in application.conf"))

  protected def tabulaSmallGroupsQueryUrl: String = tabulaSmallGroupsQuery

  private val tabulaMemberBaseUrl = configuration.getOptional[String]("mywarwick.tabula.member.base")
    .getOrElse(throw new IllegalStateException("Configuration missing - check mywarwick.tabula.member.base in application.conf"))

  protected def tabulaMemberUrl(user: User) = s"$tabulaMemberBaseUrl/${user.universityId.getOrElse(throw new IllegalArgumentException).string}"

  protected def tabulaAudienceLookUpUrl = s"$tabulaAPIBaseUrl/usercodeSearch"

  private val tabulaMemberRelationshipsSuffix = configuration.getOptional[String]("mywarwick.tabula.member.relationshipsSuffix")
    .getOrElse(throw new IllegalStateException("Configuration missing - check mywarwick.tabula.member.relationshipsSuffix in application.conf"))

  protected def tabulaMemberRelationshipsUrl(member: UniversityID) = s"$tabulaMemberBaseUrl/${member.string}$tabulaMemberRelationshipsSuffix"

}

object TabulaResponseParsers {
  val usercodesResultReads: Reads[Seq[Usercode]] = (__ \ "usercodes").read[Seq[String]].map(_.map(Usercode))

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
  val tabulaUserDataReads: Reads[TabulaUserData] = Json.reads[TabulaUserData]
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
