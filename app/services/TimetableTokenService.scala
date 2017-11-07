package services

import javax.inject.Singleton

import com.google.inject.{ImplementedBy, Inject}
import org.apache.commons.lang3.RandomStringUtils
import play.api.db.{Database, NamedDatabase}
import services.dao.{TimetableToken, TimetableTokenDao}
import warwick.sso.Usercode

@ImplementedBy(classOf[TimetableTokenServiceImpl])
trait TimetableTokenService {
  def create(usercode: Usercode): TimetableToken

  def validate(token: String): Option[Usercode]
}

@Singleton
class TimetableTokenServiceImpl @Inject()(
  @NamedDatabase("default") db: Database,
  dao: TimetableTokenDao
) extends TimetableTokenService {
  override def create(usercode: Usercode): TimetableToken = {
    val token = RandomStringUtils.randomAlphanumeric(36)
    db.withConnection(implicit c => dao.create(usercode, token))
  }

  override def validate(token: String): Option[Usercode] =
    db.withConnection(implicit c => dao.validate(token))
}

