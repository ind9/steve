package dao

import java.util.Date

import com.github.tminglei.slickpg._
import com.google.inject.{Inject, Singleton}
import dao.StevePostgresProfile.api._
import domain.Job

import scala.concurrent.Future

class JobTable(tag: Tag) extends Table[Job](tag, Some("public"), "job"){
  def id = column[Long]("id", O.AutoInc, O.PrimaryKey)
  def appName = column[String]("app_name")
  def status = column[String]("status")
  def createdAt = column[Date]("created_at", O.Default(new Date()), O.SqlType("timestamp not null default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP"))
  def updatedAt = column[Option[Date]]("updated_at")
  def attributes = column[Map[String, String]]("attributes")

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

  def update(updatedJob: Job): Future[Int] = {
    db.run {
      this.filter(_.id === updatedJob.id).update(updatedJob)
    }
  }
}
