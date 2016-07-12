package services.dao

import java.util.UUID

import anorm.SqlParser._
import anorm._
import helpers.{Fixtures, OneStartAppPerSuite}
import models.PublishedNotificationSave
import org.scalatestplus.play.PlaySpec
import warwick.sso.Usercode

class PublishedNotificationsDaoTest extends PlaySpec with OneStartAppPerSuite {

  val dao = get[PublishedNotificationsDao]

  "PublishedNotificationsDao" should {

    val custard = Usercode("custard")

    "save a published notification" in transaction { implicit c =>
      SQL"INSERT INTO PUBLISHER (ID, NAME) VALUES ('publisher-id', 'Test Publisher')"
        .execute()

      val id = get[ActivityDao].save(Fixtures.activitySave.submissionDue, Nil)

      dao.save(PublishedNotificationSave(
        activityId = id,
        publisherId = "publisher-id",
        createdBy = custard
      ))

      SQL"SELECT COUNT(*) FROM PUBLISHED_NOTIFICATION WHERE ACTIVITY_ID = $id AND PUBLISHER_ID = 'publisher-id'"
        .executeQuery()
        .as(scalar[Int].single) must equal(1)
    }

    "get notifications published by publisher" in transaction { implicit c =>
      SQL"INSERT INTO PUBLISHER (ID, NAME) VALUES ('publisher-id', 'Test Publisher')"
        .execute()

      val id = get[ActivityDao].save(Fixtures.activitySave.submissionDue, Nil)
      val id2 = get[ActivityDao].save(Fixtures.activitySave.submissionDue, Nil)

      SQL"INSERT INTO PUBLISHED_NOTIFICATION (ACTIVITY_ID, PUBLISHER_ID, CREATED_BY, CREATED_AT) VALUES ($id, 'publisher-id', 'custard', SYSDATE)"
        .execute()
      SQL"INSERT INTO PUBLISHED_NOTIFICATION (ACTIVITY_ID, PUBLISHER_ID, CREATED_BY, CREATED_AT) VALUES ($id2, 'publisher-id', 'custard', SYSDATE)"
        .execute()

      val publishedNotifications = dao.getByPublisherId("publisher-id")
      publishedNotifications.map(_.activityId) must contain only(id, id2)
    }

  }

}
