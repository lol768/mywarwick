package controllers

import play.api.data.{Form, FormError}

package object admin {

  def addFormErrors[A](form: Form[A], errors: Seq[FormError]) = errors.foldLeft(form)(_.withError(_))

}
