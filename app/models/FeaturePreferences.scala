package models

import org.joda.time.DateTime

/**
  * Information about a user's preferences relating to feature flags.
  * Currently just holds whether they have chosen to enable the Early
  * Access Program, and whether it's still active.
  */
case class FeaturePreferences(eapUntil: Option[DateTime]) {
  /**
    * When enabling EAP it is stored as an expiry date, so
    * to check if EAP is actually enabled we check that the
    * date exists and is in the future 👩‍🚀.
    * @return Whether EAP is still enabled.
    */
  def eap: Boolean = eapUntil.exists(_.isAfterNow)
}

object FeaturePreferences {
  val empty = FeaturePreferences(
    eapUntil = None
  )
}
