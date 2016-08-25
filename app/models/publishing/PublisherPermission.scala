package models.publishing

import warwick.sso.Usercode

case class PublisherPermission(
  usercode: Usercode,
  role: Role
)
