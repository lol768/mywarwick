package models

import org.joda.time.DateTime

case class UserTile(
  userCode: String,
  tileId: String,
  createdAt: DateTime,
  updatedAt: DateTime,
  removed: Boolean,
  preferences: Option[String]
)