package dao

import java.util.Date

import com.google.inject.{Inject, Singleton}
import dao.StevePostgresProfile.api._
import domain.Job
import play.api.libs.json.JsValue
import slick.sql.SqlProfile.ColumnOption

import scala.concurrent.Future

class JobTable(tag: Tag) extends Table[Job](tag, Some("public"), "job"){
  def id = column[Long]("id", O.AutoInc, O.PrimaryKey)
  def appName = column[String]("app_name")
  def status = column[String]("status")
  def createdAt = column[Date]("created_at", O.SqlType("timestamp default now()"))
  def updatedAt = column[Date]("updated_at")
  def attributes = column[JsValue]("attributes")

  def * = (id, appName, status, createdAt, updatedAt, attributes) <> (Job.tupled, Job.unapply)
}

//object Jobs extends TableQuery(new JobTable(_)) {
//  def byId(ids: Long*) = Jobs.filter(_.id inSetBind ids).map(t => t)
//}

//abstract class AbstractDAO {
 // val db = Database.forConfig("steveDatasource")
//}

@Singleton
class Jobs @Inject()(db: Database) extends TableQuery(new JobTable(_)) {

  def insert(jobs: List[Job]) = try {
   // val toBeInserted = jobs.map(job => Jobs += job)
    /*Await.result(db.run {
      DBIO.seq(
        MTable.getTables map (tables => {
          if (!tables.exists(_.name.name == this.baseTableRow.tableName))
            db.run(this.schema.create)
        }))

    }, Duration.Inf)*/

    val toBeInserted = this ++= jobs
    db.run {
      DBIO.seq(toBeInserted)
    }
  }

  def select(jobId: Long): Future[Option[Job]] = {
    db.run {
      this.filter(_.id === jobId).result.headOption
    }
  }
}
