package views

object ViewHelpers {
  import views.html.helper.FieldConstructor
  implicit val myFields = FieldConstructor(views.html.customFieldConstructor.apply _)
}
