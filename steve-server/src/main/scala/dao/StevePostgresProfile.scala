package dao

import com.github.tminglei.slickpg._
import slick.basic.Capability
import slick.jdbc.JdbcCapabilities

trait StevePostgresProfile extends ExPostgresProfile
  with PgArraySupport
  with PgHStoreSupport
  with PgDate2Support {
  def pgjson = "jsonb" // jsonb support is in postgres 9.4.0 onward; for 9.3.x use "json"

  // Add back `capabilities.insertOrUpdate` to enable native `upsert` support; for postgres 9.5+
  override protected def computeCapabilities: Set[Capability] =
    super.computeCapabilities + JdbcCapabilities.insertOrUpdate

  override val api = StevePostgresAPI

  object StevePostgresAPI extends API with ArrayImplicits
    with DateTimeImplicits
    with HStoreImplicits {
    implicit val strListTypeMapper = new SimpleArrayJdbcType[String]("text").to(_.toList)
    implicit val JavaUtilDateMapper =
      MappedColumnType.base[java.util.Date, java.sql.Timestamp](
        d => new java.sql.Timestamp(d.getTime),
        d => new java.util.Date(d.getTime))
  }

}

object StevePostgresProfile extends StevePostgresProfile
