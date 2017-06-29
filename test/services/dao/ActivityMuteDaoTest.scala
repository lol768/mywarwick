package services.dao

import anorm._
import helpers.{BaseSpec, OneStartAppPerSuite}
import models._
import org.joda.time.DateTime
import warwick.sso.Usercode

class ActivityMuteDaoTest extends BaseSpec with OneStartAppPerSuite {

  private val dao = get[ActivityMuteDao]

  transaction(rollback = false) { implicit c =>
    SQL"DELETE FROM PROVIDER".execute()
    SQL"DELETE FROM PUBLISHER".execute()

    SQL"INSERT INTO PUBLISHER (ID, NAME) VALUES ('publisher', 'publisher')"
      .execute()
    SQL"INSERT INTO PROVIDER (ID, PUBLISHER_ID, SEND_EMAIL) VALUES ('providerId', 'publisher', false)"
      .execute()
  }

  "ActivityMuteDao" should {

    "create a mute and return it" in transaction { implicit c =>
      val activity = Activity(
        id = null,
        providerId = "providerId",
        `type` = "activityType",
        title = null,
        text = null,
        url = null,
        replacedBy = null,
        publishedAt = null,
        createdAt = null,
        shouldNotify = true,
        audienceId = null,
        publisherId = null
      )

      val mute = ActivityMuteSave(
        usercode = Usercode("cusfal"),
        expiresAt = Some(DateTime.now.plusDays(1)),
        activityType = Some(activity.`type`),
        providerId = Some(activity.providerId),
        tags = Seq(
          ActivityTag(
            name = "tagName",
            displayName = None,
            value = TagValue("tagValue")
          )
        )
      )

      dao.save(mute)

      val mutes = dao.mutesForActivity(activity)

      mutes must have length 1
      mutes.head.usercode.string must equal (mute.usercode.string)
      mutes.head.createdAt must not be null
      mutes.head.expiresAt.get must equal (mute.expiresAt.get)
      mutes.head.activityType must equal (mute.activityType)
      mutes.head.providerId must equal (mute.providerId)
      mutes.head.tags must have length 1
      mutes.head.tags.head.name must equal (mute.tags.head.name)
      mutes.head.tags.head.value.internalValue must equal (mute.tags.head.value.internalValue)
    }

    "allows all optional fields to be empty and returns match" in transaction { implicit c =>
      val mute = ActivityMuteSave(
        usercode = Usercode("cusfal"),
        expiresAt = None,
        activityType = None,
        providerId = None,
        tags = Nil
      )

      val id = dao.save(mute)
      id must not be null

      val activity = Activity(
        id = null,
        providerId = "providerId",
        `type` = "activityType",
        title = null,
        text = null,
        url = null,
        replacedBy = null,
        publishedAt = null,
        createdAt = null,
        shouldNotify = true,
        audienceId = null,
        publisherId = null
      )

      val mutes = dao.mutesForActivity(activity)
      mutes must have length 1
    }

    "deletes old expired mutes" in transaction { implicit c =>
      val mute = ActivityMuteSave(
        usercode = Usercode("cusfal"),
        expiresAt = Some(DateTime.now.minusDays(1)),
        activityType = None,
        providerId = None,
        tags = Nil
      )
      dao.save(mute)

      val activity = Activity(
        id = null,
        providerId = "providerId",
        `type` = "activityType",
        title = null,
        text = null,
        url = null,
        replacedBy = null,
        publishedAt = null,
        createdAt = null,
        shouldNotify = true,
        audienceId = null,
        publisherId = null
      )
      dao.mutesForActivity(activity) must have length 1

      val deleted = dao.deleteExpiredBefore(DateTime.now)
      deleted must be (1)
      dao.mutesForActivity(activity) must have length 0
    }

    "matches recipients if specified" in transaction { implicit c =>
      val mute1 = ActivityMuteSave(
        usercode = Usercode("cusfal"),
        expiresAt = Some(DateTime.now.minusDays(1)),
        activityType = None,
        providerId = None,
        tags = Nil
      )
      dao.save(mute1)
      val mute2 = ActivityMuteSave(
        usercode = Usercode("cusebr"),
        expiresAt = Some(DateTime.now.minusDays(1)),
        activityType = None,
        providerId = None,
        tags = Nil
      )
      dao.save(mute2)

      val activity = Activity(
        id = null,
        providerId = "providerId",
        `type` = "activityType",
        title = null,
        text = null,
        url = null,
        replacedBy = null,
        publishedAt = null,
        createdAt = null,
        shouldNotify = true,
        audienceId = null,
        publisherId = null
      )
      dao.mutesForActivity(activity) must have length 2
      dao.mutesForActivity(activity, Set(Usercode("cusfal"))) must have length 1
      dao.mutesForActivity(activity, Set(Usercode("cusfal"), Usercode("cusebr"))) must have length 2
    }

  }

  "expire a mute" in transaction { implicit c =>
    val activity = Activity(
      id = null,
      providerId = "providerId",
      `type` = "activityType",
      title = null,
      text = null,
      url = null,
      replacedBy = null,
      publishedAt = null,
      createdAt = null,
      shouldNotify = true,
      audienceId = null,
      publisherId = null
    )

    val mute = ActivityMuteSave(
      usercode = Usercode("cusfal"),
      expiresAt = Some(DateTime.now.plusDays(1)),
      activityType = Some(activity.`type`),
      providerId = Some(activity.providerId),
      tags = Seq(
        ActivityTag(
          name = "tagName",
          displayName = None,
          value = TagValue("tagValue")
        )
      )
    )

    val muteId = dao.save(mute)

    dao.expire(ActivityMuteRender.fromActivityMuteSave(muteId, mute))

    val expiredMute = dao.mutesForRecipient(mute.usercode)
    expiredMute must have length 1
    expiredMute.head.usercode.string must equal (mute.usercode.string)
    expiredMute.head.expiresAt.get.isBeforeNow must be (true)
  }

}
