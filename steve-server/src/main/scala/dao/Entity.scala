package dao

import java.util.{Date, UUID}

import com.google.inject.{Inject, Singleton}
import dao.StevePostgresProfile.api._
import domain.{Job,Item}

import scala.concurrent.Future

class JobTable(tag: Tag) extends Table[Job](tag, Some("public"), "job") {
  def id = column[UUID]("id", O.PrimaryKey)
  def appName = column[String]("app_name")
  def state = column[String]("state")
  def createdAt = column[Date]("created_at", O.Default(new Date()), O.SqlType("timestamp not null default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP"))
  def updatedAt = column[Option[Date]]("updated_at")
  def attributes = column[Map[String, String]]("attributes")

  def * = (id, appName, state, createdAt, updatedAt, attributes) <> (Job.tupled, Job.unapply)
}

class ItemTable(tag: Tag) extends Table[Item](tag, Some("public"), "item") {
  def id = column[UUID]("id", O.PrimaryKey)
  def jobId = column[UUID]("job_id")
  def status = column[String]("status")
  def createdAt = column[Date]("created_at", O.Default(new Date()), O.SqlType("timestamp not null default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP"))
  def updatedAt = column[Option[Date]]("updated_at")
  def attributes = column[Map[String, String]]("attributes")

  protected val jobTableQuery = TableQuery[JobTable]
  def jobForeignKey = foreignKey("JOB_ID_FK", jobId, jobTableQuery)(_.id)

  def * = (id, jobId, status, createdAt, updatedAt, attributes) <> (Item.tupled, Item.unapply)
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

  def select(jobId: UUID): Future[Option[Job]] = {
    db.run {
      this.filter(_.id === jobId).result.headOption
    }
  }

  def update(updatedJob: Job): Future[Int] = {
    db.run {
      this.filter(_.id === updatedJob.id).update(updatedJob.copy(updatedAt = Some(new Date())))
    }
  }

  def delete(jobId: UUID): Future[Int] = {
    db.run {
      this.filter(_.id === jobId).delete
    }
  }
}

@Singleton
class Items @Inject()(db: Database) extends TableQuery(new ItemTable(_)) {

  def insert(items: List[Item]) = try {
    val toBeInserted = this ++= items
    db.run {
      DBIO.seq(toBeInserted)
    }
  }

  def select(itemId: UUID): Future[Option[Item]] = {
    db.run {
      this.filter(_.id === itemId).result.headOption
    }
  }

  def update(updatedItem: Item): Future[Int] = {
    db.run {
      this.filter(_.id === updatedItem.id).update(updatedItem)
    }
  }

  def delete(itemId: UUID): Future[Int] = {
    db.run {
      this.filter(_.id === itemId).delete
    }
  }
}
