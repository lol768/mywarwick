package services.dao

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
      val id = get[ActivityDao].save(Fixtures.activitySave.submissionDue, Nil)

      dao.save(PublishedNotificationSave(
        activityId = id,
        publisherId = "default",
        changedBy = custard
      ))

      SQL"SELECT COUNT(*) FROM PUBLISHED_NOTIFICATION WHERE ACTIVITY_ID = $id AND PUBLISHER_ID = 'default'"
        .executeQuery()
        .as(scalar[Int].single) must equal(1)
    }

    "update a published notification" in transaction { implicit c =>
      val id = get[ActivityDao].save(Fixtures.activitySave.submissionDue, Nil)

      dao.save(PublishedNotificationSave(
        activityId = id,
        publisherId = "default",
        changedBy = custard
      ))

      dao.update(PublishedNotificationSave(
        activityId = id,
        publisherId = "default",
        changedBy = custard
      ))

      SQL"SELECT UPDATED_BY FROM PUBLISHED_NOTIFICATION WHERE ACTIVITY_ID = $id"
        .executeQuery()
        .as(scalar[String].single) must be("custard")
    }

    "get notifications published by publisher" in transaction { implicit c =>
      val id = get[ActivityDao].save(Fixtures.activitySave.submissionDue, Nil)
      val id2 = get[ActivityDao].save(Fixtures.activitySave.submissionDue, Nil)

      SQL"INSERT INTO PUBLISHED_NOTIFICATION (ACTIVITY_ID, PUBLISHER_ID, CREATED_BY, CREATED_AT) VALUES ($id, 'default', 'custard', SYSDATE)"
        .execute()
      SQL"INSERT INTO PUBLISHED_NOTIFICATION (ACTIVITY_ID, PUBLISHER_ID, CREATED_BY, CREATED_AT) VALUES ($id2, 'default', 'custard', SYSDATE)"
        .execute()

      val publishedNotifications = dao.getByPublisherId("default")
      publishedNotifications.map(_.activityId) must contain only(id, id2)
    }

  }

}
