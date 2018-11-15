package controllers

import controllers.Assets.Asset
import helpers.BaseSpec

class AssetsControllerTest extends BaseSpec {

  import AssetsController.removeFingerprint

  val bundle              = Asset("/assets/js/bundle.min.js")
  val bundleFingerprinted = Asset("/assets/js/371fd24396de336458eb50933f9a0aaf-bundle.min.js")
  val bundleOtherPrefix   = Asset("/assets/js/badmedicine-bundle.min.js")

  "removeFingerprint" should {

    "return None for unfingerprinted match" in {
      removeFingerprint(bundle) mustBe None
    }

    "return something for a match" in {
      removeFingerprint(bundleFingerprinted) mustBe Some(bundle)
    }

    "not just match anything before a dash" in {
      removeFingerprint(bundleOtherPrefix) mustBe None
    }

  }
}
