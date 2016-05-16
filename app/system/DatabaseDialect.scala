package system

import com.google.inject.ImplementedBy

@ImplementedBy(classOf[OracleDatabaseDialect])
trait DatabaseDialect {

  def limitOffset(limit: Int, offset: Int = 0): String

}

class OracleDatabaseDialect extends DatabaseDialect {

  override def limitOffset(limit: Int, offset: Int = 0): String =
    s"OFFSET ${offset} ROWS FETCH NEXT ${limit} ROWS ONLY"

}

class H2DatabaseDialect extends DatabaseDialect {

  override def limitOffset(limit: Int, offset: Int = 0): String =
    s"LIMIT ${limit} OFFSET ${offset}"

}
