package services.dao

import anorm._
import helpers.{BaseSpec, OneStartAppPerSuite}
import models.Audience.LocationOptIn
import warwick.sso.Usercode

class UserNewsOptInDaoTest extends BaseSpec with OneStartAppPerSuite {

  private val dao = get[UserNewsOptInDao]

  "UserNewsOptInDao" should {

    "get for usercode" in transaction { implicit c =>
      SQL"INSERT INTO USER_NEWS_OPT_IN (USERCODE, NAME, VALUE) VALUES ('cusfal', 'Location', 'Coventry')".execute()
      SQL"INSERT INTO USER_NEWS_OPT_IN (USERCODE, NAME, VALUE) VALUES ('cusfal', 'Location', 'Unknown')".execute()
      SQL"INSERT INTO USER_NEWS_OPT_IN (USERCODE, NAME, VALUE) VALUES ('cusfal', 'Unknown', 'Coventry')".execute()

      dao.get(Usercode("cusfal")) mustBe Seq(LocationOptIn.Coventry)
    }

    "save opt-in" in transaction { implicit c =>
      SQL"INSERT INTO USER_NEWS_OPT_IN (USERCODE, NAME, VALUE) VALUES ('cusfal', 'Location', 'Coventry')".execute()

      dao.get(Usercode("cusfal")) mustBe Seq(LocationOptIn.Coventry)
      dao.save(Usercode("cusfal"), LocationOptIn.optInType, Seq(LocationOptIn.CentralCampusResidences, LocationOptIn.Kenilworth))
      dao.get(Usercode("cusfal")) mustBe Seq(LocationOptIn.CentralCampusResidences, LocationOptIn.Kenilworth)
      dao.save(Usercode("cusfal"), LocationOptIn.optInType, Seq.empty)
      dao.get(Usercode("cusfal")) mustBe Seq.empty
    }

    "get for opt-in" in transaction { implicit c =>
      SQL"INSERT INTO USER_NEWS_OPT_IN (USERCODE, NAME, VALUE) VALUES ('cusfal', 'Location', 'Coventry')".execute()
      SQL"INSERT INTO USER_NEWS_OPT_IN (USERCODE, NAME, VALUE) VALUES ('cusebr', 'Location', 'Kenilworth')".execute()
      SQL"INSERT INTO USER_NEWS_OPT_IN (USERCODE, NAME, VALUE) VALUES ('cuscao', 'Other', 'Coventry')".execute()

      dao.getUsercodes(LocationOptIn.Coventry) mustBe Set(Usercode("cusfal"))
    }

  }

}
