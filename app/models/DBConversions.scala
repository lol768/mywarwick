package models

import anorm.{MetaDataItem, TypeDoesNotMatch, Column}
import oracle.sql.TIMESTAMP
import org.joda.time.DateTime

trait DBConversions {
  implicit def columnToDateTime: Column[DateTime] =
    Column.nonNull1 { (value, meta) =>
      val MetaDataItem(qualified, nullable, clazz) = meta
      value match {
        case timestamp: TIMESTAMP => Right(new DateTime(timestamp.timestampValue()))
        case _                    => Left(TypeDoesNotMatch(s"Cannot convert $value: ${value.asInstanceOf[AnyRef].getClass} to DateTime for column $qualified"))
      }
    }
}
