package models

import org.joda.time.{DateTime, DateTimeZone}
import org.scalatestplus.play.PlaySpec

class DateFormatsTest extends PlaySpec {

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

  }


}
