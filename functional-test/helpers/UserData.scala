package helpers

import com.typesafe.config.Config

/**
  *
  */
case class UserData(username: String, password: String)

object UserData {
  def apply(c: Config): UserData = UserData(
    c.getString("username"),
    c.getString("password")
  )
}