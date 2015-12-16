package services.messaging

/**
  * Case object enum for how a message is to be sent out.
  */
sealed abstract class Output(val name: String)
object Output {
  val values = Set(Email, SMS, Mobile)
  def parse(name: String) = unapply(name).getOrElse(throw new IllegalArgumentException)
  def unapply(name: String) = values.find(_.name == name)

  case object Email extends Output("email")
  case object SMS extends Output("sms")
  case object Mobile extends Output("mobile")
}
