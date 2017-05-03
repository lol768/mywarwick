import org.scalatest.Tag

/**
  * Tags that are attached to tests so that we can run subsets of the tests.
  */

object MobileTest extends Tag("uk.ac.warwick.MobileTest")
object DesktopTest extends Tag("uk.ac.warwick.DesktopTest")
object AndroidTest extends Tag("uk.ac.warwick.AndroidTest")
object IosTest extends Tag("uk.ac.warwick.IosTest")

object ApiTest extends Tag("uk.ac.warwick.ApiTest")