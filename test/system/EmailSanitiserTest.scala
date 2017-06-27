package system

import helpers.BaseSpec


class EmailSanitiserTest extends BaseSpec  {

  "EmailSanitiser" should {
    "leave alone a benign subject" in {
      val subj = "New MyWarwick Notification"
      EmailSanitiser.sanitiseUserInputForHeader(subj) mustBe subj
    }

    "totally strip a suspicious subject" in {
      val subj = "New\r\n MyWarwick\r\n Notification"
      EmailSanitiser.sanitiseUserInputForHeader(subj) mustBe "New MyWarwick Notification"
    }
  }
}
