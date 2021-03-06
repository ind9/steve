package dao

import java.util.Date

import com.google.inject.{Inject, Singleton}
import dao.StevePostgresProfile.api._
import domain.{Item, Job}
import slick.ast.BaseTypedType
import slick.lifted.AbstractTable

import scala.concurrent.{ExecutionContext, Future}

class JobTable(tag: Tag) extends Table[Job](tag, Some("public"), "job") {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

  def appName = column[String]("app_name")

  def state = column[String]("state")

  def createdAt = column[Date]("created_at", O.Default(new Date()), O.SqlType("timestamp not null default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP"))

  def updatedAt = column[Option[Date]]("updated_at")

  def attributes = column[Map[String, String]]("attributes")

  def * = (id, appName, state, createdAt, updatedAt, attributes) <> (Job.tupled, Job.unapply)
}

class ItemTable(tag: Tag) extends Table[Item](tag, Some("public"), "item") {
  def id = column[String]("id")

  def jobId = column[Long]("job_id")

  def status = column[String]("status")

  def createdAt = column[Date]("created_at", O.Default(new Date()), O.SqlType("timestamp not null default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP"))

  def updatedAt = column[Option[Date]]("updated_at")

  def attributes = column[Map[String, String]]("attributes")

  protected val jobTableQuery = TableQuery[JobTable]

  def pKey = primaryKey("item_pkey", (id, jobId))

  def jobForeignKey = foreignKey("item_job_id_fkey", jobId, jobTableQuery)(_.id, onDelete = ForeignKeyAction.Cascade)

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
    db.run {
      table ++= entities
    }
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
class Jobs @Inject()(db: Database, implicit val ec: ExecutionContext) extends GenericDAO[JobTable, Long](db) {

  val table: TableQuery[JobTable] = TableQuery[JobTable]

  override def getId(row: JobTable): Rep[Long] = row.id

  val insertQuery = table returning table.map(_.id) into ((job, id) => job.copy(id = id))

  def insertSelect(entity: Job): Future[Job] = {
    db.run {
      insertQuery += entity.copy(id = 0)
    }
  }

  def getJobIdsByState(state: String): Future[Seq[Long]] = {
    db.run {
      table.filter(_.state === state).map(c => c.id).result
    }
  }

  def update(updatedJob: Job): Future[Int] = {
    db.run {
      table.filter(_.id === updatedJob.id).update(updatedJob.copy(updatedAt = Some(new Date())))
    }
  }

  def updateStateById(id: Long, state: String): Future[Int] = {
    db.run {
      table.filter(_.id === id).map(c => c.state).update(state)
    }
  }

}

@Singleton
class Items @Inject()(db: Database) extends GenericDAO[ItemTable, String](db) {

  val table: TableQuery[ItemTable] = TableQuery[ItemTable]

  override def getId(row: ItemTable): Rep[String] = row.id

  def checkIfStatusNotPresent(jobId: Long, status: String): Future[Boolean] = {
    db.run {
      table.filter(_.jobId === jobId).filterNot(_.status === status).exists.result
    }
  }

  def checkIfStatusPresent(jobId: Long, status: String): Future[Boolean] = {
    db.run {
      table.filter(_.jobId === jobId).filter(_.status === status).exists.result
    }
  }

  def update(updatedItem: Item): Future[Int] = {
    db.run {
      table.filter(_.id === updatedItem.id).update(updatedItem.copy(updatedAt = Some(new Date())))
    }
  }

  def updateStatusByJobId(jobId: Long, status: String): Future[Int] = {
    db.run {
      table.filter(_.jobId === jobId).map(c => c.status).update(status)
    }
  }

  private def filterById(id: Option[String], query: Query[ItemTable, ItemTable#TableElementType, scala.Seq]) = id.map(idVal => query.filter(_.id === idVal)).getOrElse(query)

  private def filterByJobId(jobId: Option[Long], query: Query[ItemTable, ItemTable#TableElementType, scala.Seq]) = jobId.map(jobIdVal => query.filter(_.jobId === jobIdVal)).getOrElse(query)

  private def filterByStatus(status: Option[String], query: Query[ItemTable, ItemTable#TableElementType, scala.Seq]) = status.map(statusVal => query.filter(_.status === statusVal)).getOrElse(query)

  private def filterByAttributes(attributes: Map[String, String], table: StevePostgresProfile.api.TableQuery[ItemTable]) = attributes match {
    case attr if attr == null || attr.isEmpty => table
    case attr => table.filter(f => attr.map {
      case (key, value) => f.attributes.+>(key.bind) === value.bind
    }.reduceLeft(_ && _))
  }


  def updateStatus(id: Option[String], jobId: Option[Long], queryStatus: Option[String], attributes: Map[String, String], updateStatus: String): Future[Int] = {
    db.run {
      //TODO: Check if there's a better way to get the below done?!
      filterById(id,
        filterByJobId(jobId,
          filterByStatus(queryStatus,
            filterByAttributes(attributes, table)))).map(c => c.status).update(updateStatus)
    }
  }

  // TODO refactor and move this to a nicer place.
  def stats(jobId: Long): Future[_] = {
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

  def stats(attributeKey: String, attributeValue: String, fromDate: Date, toDate: Option[Date]): Future[_] = {
    db.run {
      table.filter(_.attributes.+>(attributeKey) === attributeValue)
        .filter(_.createdAt >= fromDate)
        .filter(r => toDate.fold(true.bind)(r.createdAt <= _))
        .groupBy(_.status)
        .map {
          case (id, group) => (id, group.map(_.status).length)
        }
        .to[List]
        .result
    }
  }

  def statsAttrPattern(attributeKey: String, attributeValuePattern: String, fromDate: Date, toDate: Option[Date]): Future[_] = {
    db.run {
      table.filter(_.attributes.+>(attributeKey) like s"%$attributeValuePattern%")
        .filter(_.createdAt >= fromDate)
        .filter(r => toDate.fold(true.bind)(r.createdAt <= _))
        .groupBy(_.status)
        .map {
          case (id, group) => (id, group.map(_.status).length)
        }
        .to[List]
        .result
    }
  }
}
