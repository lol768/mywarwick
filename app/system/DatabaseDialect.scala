package system

import com.google.inject.ImplementedBy

/**
  * Vendor-specific SQL dialect stuff that we can't really work around.
  * The application will wire in the Oracle version by default, which tests
  * override to use H2 (the embedded test database)
  */
@ImplementedBy(classOf[OracleDatabaseDialect])
trait DatabaseDialect {

  def limitOffset(limit: Int, offset: Int = 0)(body: String): String
  def limitOffset(o: SqlPage)(body: String): String = limitOffset(o.limit, o.offset)(body)

}

class OracleDatabaseDialect extends DatabaseDialect {

  override def limitOffset(limit: Int, offset: Int = 0)(body: String) : String =
    s"$body OFFSET $offset ROWS FETCH NEXT $limit ROWS ONLY"

}

class H2DatabaseDialect extends DatabaseDialect {

  override def limitOffset(limit: Int, offset: Int = 0)(body: String): String =
    s"$body LIMIT $limit OFFSET $offset"

}
