package services.dao

import anorm._
import helpers.OneStartAppPerSuite
import models.publishing.PublishingRole.{NewsManager, NotificationsManager}
import org.scalatestplus.play.PlaySpec
import warwick.sso.Usercode

class PublisherDaoTest extends PlaySpec with OneStartAppPerSuite {

  val dao = get[PublisherDao]

  "PublisherDao" should {

    val custard = Usercode("custard")

    "get all publishers" in transaction { implicit c =>
      SQL"INSERT INTO PUBLISHER (ID, NAME) VALUES ('publisher-id', 'Test Publisher')"
        .execute()

      SQL"INSERT INTO PUBLISHER (ID, NAME) VALUES ('another-publisher-id', 'Another Test Publisher')"
        .execute()

      dao.all.map(_.id) must contain allOf("publisher-id", "another-publisher-id")
    }

    "find publisher by ID" in transaction { implicit c =>
      SQL"INSERT INTO PUBLISHER (ID, NAME) VALUES ('publisher-id', 'Test Publisher')"
        .execute()

      val publisher = dao.find("publisher-id")
      publisher must not be empty
      publisher.get must have ('id ("publisher-id"), 'name ("Test Publisher"))

      dao.find("non-existent-publisher-id") mustBe empty
    }

    "get permissions for publisher by usercode" in transaction { implicit c =>
      SQL"INSERT INTO PUBLISHER (ID, NAME) VALUES ('publisher-id', 'Test Publisher')"
        .execute()

      SQL"INSERT INTO PUBLISHER_PERMISSION (PUBLISHER_ID, USERCODE, ROLE) VALUES ('publisher-id', 'custard', 'NewsManager')"
        .execute()

      SQL"INSERT INTO PUBLISHER_PERMISSION (PUBLISHER_ID, USERCODE, ROLE) VALUES ('publisher-id', 'custard', 'NotificationsManager')"
        .execute()

      val permissions = dao.getPublisherPermissions("publisher-id", custard)

      permissions.map(_.role) must contain allOf(NewsManager, NotificationsManager)

      dao.getPublisherPermissions("publisher-id", Usercode("cusbob")) mustBe empty
    }

  }

}
