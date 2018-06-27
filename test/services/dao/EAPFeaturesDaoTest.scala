package services.dao

import helpers.{BaseSpec, OneStartAppPerSuite}
import models._
import org.joda.time.LocalDate

class EAPFeaturesDaoTest extends BaseSpec with OneStartAppPerSuite {

  private val dao = get[EAPFeaturesDao]

  "ActivityDao" should {

    "save a new feature" in transaction { implicit c =>
      val feature = EAPFeatureSave(
        "Feature",
        Some(LocalDate.now),
        Some(LocalDate.now.plusDays(1)),
        Some("Summary"),
        Some("url")
      )
      val id = dao.save(feature)
      val fetchedFeature = dao.getById(id).get
      fetchedFeature.id mustBe id
      fetchedFeature.name mustBe feature.name
      fetchedFeature.startDate mustBe feature.startDate
      fetchedFeature.endDate mustBe feature.endDate
      fetchedFeature.summaryRaw mustBe feature.summaryRaw
      fetchedFeature.feedbackUrl mustBe feature.feedbackUrl
    }

    "update feature" in transaction { implicit c =>
      val oldFeature = EAPFeatureSave(
        "Feature",
        Some(LocalDate.now),
        Some(LocalDate.now.plusDays(1)),
        Some("Summary"),
        Some("url")
      )
      val id = dao.save(oldFeature)

      val newFeature = oldFeature.copy(
        name = "New feature",
        startDate = None,
        endDate = None,
        summaryRaw = None,
        feedbackUrl = None
      )
      dao.update(id, newFeature)

      val fetchedFeature = dao.getById(id).get
      fetchedFeature.id mustBe id
      fetchedFeature.name mustBe newFeature.name
      fetchedFeature.startDate mustBe newFeature.startDate
      fetchedFeature.endDate mustBe newFeature.endDate
      fetchedFeature.summaryRaw mustBe newFeature.summaryRaw
      fetchedFeature.feedbackUrl mustBe newFeature.feedbackUrl
    }

    "delete feature" in transaction { implicit c =>
      val oldFeature = EAPFeatureSave(
        "Feature",
        Some(LocalDate.now),
        Some(LocalDate.now.plusDays(1)),
        Some("Summary"),
        Some("url")
      )
      val id = dao.save(oldFeature)

      dao.delete(id)

      val fetchedFeature = dao.getById(id)
      fetchedFeature mustBe None
    }

  }
}
