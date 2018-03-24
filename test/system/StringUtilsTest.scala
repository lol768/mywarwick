package system

import helpers.BaseSpec

class StringUtilsTest extends BaseSpec {

  import StringUtils.{truncateToBytes => truncate}

  // This is 3 bytes in UTF-8
  private final val TRUTH = "䷼"

  "truncate" should {
    "return strings unchanged" in {
      truncate(100, "Regular input") must be("Regular input")
    }

    "truncate simple strings like substring" in {
      truncate(10, "This is a long sentence") must be("This is a ")
    }

    "truncate 3-byte char correctly correctly" in {
      truncate(9, s"Truth:$TRUTH!") must be (s"Truth:$TRUTH")
    }

    "not leave bits of UTF-8 byte left over" in {
      truncate(10, s"aaaaa$TRUTH$TRUTH") must be (s"aaaaa$TRUTH")
    }

    // This demonstrates known wrong behaviour -
    // The output is 3 bytes longer than the max length.
    // We only need to handle text that's mostly English so
    // it's a reasonable optimisation to assume that the
    // byte count won't be more than twice the char count.
    "not return too many bytes but it does anyway" in {
      val threeTruths = s"$TRUTH$TRUTH$TRUTH"
      truncate(6, threeTruths) must be (threeTruths)
    }
  }

}
