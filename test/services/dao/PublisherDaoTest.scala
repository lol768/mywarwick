package services.dao

import anorm._
import helpers.OneStartAppPerSuite
import models.publishing.PublishingRole.{NewsManager, NotificationsManager}
import helpers.BaseSpec
import services.ProviderSave
import warwick.sso.Usercode

class PublisherDaoTest extends BaseSpec with OneStartAppPerSuite {

  private val dao = get[PublisherDao]

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

    "custard(non-publisher user) should not be a publisher" in transaction { implicit  c =>
      dao.isPublisher(custard.string) mustBe false
    }

    "shylock(legitimate publisher) should be identified as a publisher" in transaction { implicit  c=>

      SQL"INSERT INTO PUBLISHER (ID, NAME) VALUES ('publisher-id', 'Test Publisher')"
        .execute()
      SQL"INSERT INTO PUBLISHER_PERMISSION (PUBLISHER_ID, USERCODE, ROLE) VALUES ('publisher-id', 'shylock', 'NewsManager')"
        .execute()

      dao.isPublisher("shylock") mustBe true

    }

    "save provider" in transaction { implicit c =>
      SQL"INSERT INTO PUBLISHER (ID, NAME) VALUES ('publisher-id', 'Test Publisher')"
        .execute()

      val provider = ProviderSave(
        name = Some("Provider name"),
        icon = Some("fa-sun"),
        colour = Some("00ff00"),
        sendEmail = true,
        overrideMuting = true
      )

      dao.saveProvider("publisher-id", "provider-id", provider)

      val providers = dao.getProviders("publisher-id")
      providers.exists(_.id == "provider-id") must be (true)
      providers.find(_.id == "provider-id").get.name must be (provider.name)
      providers.find(_.id == "provider-id").get.icon must be (provider.icon)
      providers.find(_.id == "provider-id").get.colour must be (provider.colour)
      providers.find(_.id == "provider-id").get.sendEmail must be (provider.sendEmail)
      providers.find(_.id == "provider-id").get.overrideMuting must be (provider.overrideMuting)
    }

    "update provider" in transaction { implicit c =>
      SQL"INSERT INTO PUBLISHER (ID, NAME) VALUES ('publisher-id', 'Test Publisher')"
        .execute()

      SQL"INSERT INTO PROVIDER VALUES ('provider-id', 'Test Provider', 'fa-clock', '000000', 'publisher-id', 0, 0, 0)"
        .execute()

      val provider = ProviderSave(
        name = Some("Provider name"),
        icon = Some("fa-sun"),
        colour = Some("00ff00"),
        sendEmail = true,
        overrideMuting = true
      )

      dao.updateProvider("publisher-id", "provider-id", provider)

      val providers = dao.getProviders("publisher-id")
      providers.exists(_.id == "provider-id") must be (true)
      providers.find(_.id == "provider-id").get.name must be (provider.name)
      providers.find(_.id == "provider-id").get.icon must be (provider.icon)
      providers.find(_.id == "provider-id").get.colour must be (provider.colour)
      providers.find(_.id == "provider-id").get.sendEmail must be (provider.sendEmail)
      providers.find(_.id == "provider-id").get.overrideMuting must be (provider.overrideMuting)
    }

  }

}
