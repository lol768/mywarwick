package helpers

import org.scalatest._
import org.scalatest.concurrent.ScalaFutures

abstract class BaseSpec
  extends WordSpec
    with MustMatchers
    with OptionValues
    with ScalaFutures
