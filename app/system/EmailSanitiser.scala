package system

import scala.util.matching.Regex

object EmailSanitiser {

  lazy val regex: Regex = "\n|\r".r

  def sanitiseUserInputForHeader(input: String): String = {
    regex.replaceAllIn(input, "")
  }

}
