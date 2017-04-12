package models

import org.joda.time.{DateTime, DateTimeZone}
import helpers.BaseSpec

class DateFormatsTest extends BaseSpec {

  import DateFormats._

  "DateFormats.isoDateWrites" should {

    val london = DateTimeZone.forID("Europe/London")

    "write dates in Greenwich Mean Time as UTC" in {
      val dateGMT = new DateTime(2016, 1, 1, 9, 0, london)
      isoDateWrites.writes(dateGMT).as[String] must be("2016-01-01T09:00:00.000Z")
    }

    "write dates in British Summer Time as UTC" in {
      val dateBST = new DateTime(2016, 6, 1, 9, 0, london)
      isoDateWrites.writes(dateBST).as[String] must be("2016-06-01T08:00:00.000Z")
    }

    "write friendly date correctly" in {
      val fourthOfJuly = new DateTime(2016, 7, 4, 10, 0, london)
      fourthOfJuly.toString mustBe "2016-07-04T10:00:00.000+01:00"
      emailDateTime(fourthOfJuly) mustBe "10:00 on Mon 4th Jul, 2016"
    }

  }


}
