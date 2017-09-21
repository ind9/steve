package dao

import java.util.{Date, UUID}

import com.google.inject.{Inject, Singleton}
import dao.StevePostgresProfile.api._
import domain.{Item, Job}
import slick.ast.BaseTypedType
import slick.lifted.AbstractTable

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

  def jobForeignKey = foreignKey("JOB_ID_FK", jobId, jobTableQuery)(_.id, onDelete=ForeignKeyAction.Cascade)

  def * = (id, jobId, status, createdAt, updatedAt, attributes) <> (Item.tupled, Item.unapply)
}

abstract class GenericDAO[T <: AbstractTable[_], I: BaseTypedType](db: Database) {

  type Id = I

  def table: TableQuery[T]

  def getId(row: T): Rep[Id]

  def insert(entity: T#TableElementType): Future[Int] = {
    db.run {
      table += entity
    }
  }

  def insert(entities: List[T#TableElementType]): Future[Option[Int]] = {
    db.run{table ++= entities}
  }

  def select(id: Id): Future[Option[_]] = {
    db.run {
      table.filter(getId(_) === id).result.headOption
    }
  }

  private def buildDeleteAction(id: Id) = {
    slickProfile.createDeleteActionExtensionMethods(
      slickProfile.deleteCompiler.run(table.filter(getId(_) === id).toNode).tree, ()
    )
  }

  def delete(id: Id): Future[Int] = {
    val deleteAction = buildDeleteAction(id)
    db.run {
      deleteAction.delete
    }
  }

}

@Singleton
class Jobs @Inject()(db: Database) extends GenericDAO[JobTable,UUID](db) {

  val table: TableQuery[JobTable] = TableQuery[JobTable]

  override def getId(row: JobTable): Rep[UUID] = row.id

  def update(updatedJob: Job): Future[Int] = {
    db.run {
      table.filter(_.id === updatedJob.id).update(updatedJob.copy(updatedAt = Some(new Date())))
    }
  }

}

@Singleton
class Items @Inject()(db: Database) extends GenericDAO[ItemTable,UUID](db) {

  val table: TableQuery[ItemTable] = TableQuery[ItemTable]

  override def getId(row: ItemTable): Rep[UUID] = row.id

  def update(updatedItem: Item): Future[Int] = {
    db.run {
      table.filter(_.id === updatedItem.id).update(updatedItem.copy(updatedAt = Some(new Date())))
    }
  }

  // TODO refactor and move this to a nicer place.
  def stats(jobId: UUID): Future[_] = {
    db.run {
      table.filter(_.jobId === jobId)
        .groupBy(_.status)
        .map {
          case (id, group) => (id, group.map(_.status).length)
        }
        .to[List]
        .result
    }
  }
}
