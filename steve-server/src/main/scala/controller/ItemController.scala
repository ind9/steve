package controller

import java.util.{Date, UUID}
import javax.ws.rs._
import javax.ws.rs.container.{AsyncResponse, Suspended}
import javax.ws.rs.core.Response.Status
import javax.ws.rs.core.{Context, MediaType, Response, UriInfo}

import com.google.inject.Inject
import dao.Items
import domain.Item
import steve.SteveConfiguration

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}
import scala.collection.JavaConverters._

@Path("/item")
class ItemController @Inject()(steveConfiguration: SteveConfiguration, items: Items, implicit val ec: ExecutionContext) {

  @PUT
  @Path("/")
  @Produces(Array(MediaType.APPLICATION_JSON))
  def createItem(item: Item, @Suspended res: AsyncResponse) = {
    val newItem: Item = item.copy(id = UUID.randomUUID, createdAt = new Date())
    items.insert(newItem).onComplete {
      case Success(_) => res.resume(Response.status(Status.CREATED).entity(Map("id" -> newItem.id, "msg" -> "Created")).build())
      case Failure(error) => {error.printStackTrace(); res.resume(Response.status(Status.INTERNAL_SERVER_ERROR).entity(Map("msg" -> error.getMessage)).build())}
    }
  }

  @PUT
  @Path("/bulk")
  @Produces(Array(MediaType.APPLICATION_JSON))
  def createItems(itemList: List[Item], @Suspended res: AsyncResponse) = {
    val newItems = itemList.map(item => item.copy(id = UUID.randomUUID, createdAt = new Date()))
    items.insert(newItems).onComplete {
      case Success(_) => res.resume(Response.status(Status.CREATED).entity(Map("msg" -> "Created")).build())
      case Failure(error) => {error.printStackTrace(); res.resume(Response.status(Status.INTERNAL_SERVER_ERROR).entity(Map("msg" -> error.getMessage)).build())}
    }
  }

  @GET
  @Path("/{id}")
  @Produces(Array(MediaType.APPLICATION_JSON))
  def getItem(@PathParam("id") itemId: UUID, @Suspended res: AsyncResponse) = {
    items.select(itemId).onComplete {
      case Success(Some(item)) => res.resume(Response.status(Status.OK).entity(item).build())
      case Success(None) => res.resume(Response.status(Status.NOT_FOUND).entity(Map("id" -> itemId, "msg" -> "Not Found")).build())
      case Failure(error) => res.resume(Response.status(Status.INTERNAL_SERVER_ERROR).entity(Map("msg" -> error.getMessage)).build())
    }
  }

  @POST
  @Path("/{id}")
  @Produces(Array(MediaType.APPLICATION_JSON))
  def updateItem(@PathParam("id") itemId: UUID, item: Item, @Suspended res: AsyncResponse) = {
    val updatedItem: Item = item.copy(id = itemId, updatedAt = Some(new Date()))
    items.update(updatedItem).onComplete {
      case Success(_) => res.resume(Response.status(Status.OK).entity(updatedItem).build())
      case Failure(error) => res.resume(Response.status(Status.INTERNAL_SERVER_ERROR).entity(Map("msg" -> error.getMessage)).build())
    }
  }

  @POST
  @Path("/{id}/status")
  @Produces(Array(MediaType.APPLICATION_JSON))
  def updateItemStatusById(@PathParam("id") itemId: UUID, status: String, @Suspended res: AsyncResponse) = {
    val id = Option(itemId)
    items.updateStatus(id, None, None, Map(), status).onComplete {
      case Success(_) => res.resume(Response.status(Status.OK).entity(Map("msg" -> "Updated")).build())
      case Failure(error) => res.resume(Response.status(Status.INTERNAL_SERVER_ERROR).entity(Map("msg" -> error.getMessage)).build())
    }
  }

  @POST
  @Path("/status")
  @Produces(Array(MediaType.APPLICATION_JSON))
  def updateItemStatus(@Context info: UriInfo, status: String, @Suspended res: AsyncResponse) = {
    val id = Option(info.getQueryParameters.getFirst("id")).map(id => UUID.fromString(id))
    val jobId = Option(info.getQueryParameters.getFirst("jobId")).map(id => UUID.fromString(id))
    val queryStatus = Option(info.getQueryParameters.getFirst("status"))
    //Taking only the first value in the list of values if passed, for the attributes
    //TODO: May be throw bad request if the above contract is breached?
    val attributes = info.getQueryParameters.asScala.-("id").-("jobId").-("status").map { case (key, value) if value.size() > 0 => (key, value.get(0)) }.toMap
    items.updateStatus(id, jobId, queryStatus, attributes, status).onComplete {
      case Success(_) => res.resume(Response.status(Status.OK).entity(Map("msg" -> "Updated")).build())
      case Failure(error) => res.resume(Response.status(Status.INTERNAL_SERVER_ERROR).entity(Map("msg" -> error.getMessage)).build())
    }
  }

  @DELETE
  @Path("/{id}")
  @Produces(Array(MediaType.APPLICATION_JSON))
  def deleteItem(@PathParam("id") itemId: UUID, @Suspended res: AsyncResponse) = {
    items.delete(itemId).onComplete {
      case Success(rowsDeleted) if rowsDeleted == 0 => res.resume(Response.status(Status.NOT_FOUND).entity(Map("id" -> itemId, "msg" -> "Not Found")).build())
      case Success(rowsDeleted) => res.resume(Response.status(Status.OK).entity(Map("id" -> itemId, "msg" -> s"Deleted", "rowsAffected" -> rowsDeleted)).build())
      case Failure(error) => res.resume(Response.status(Status.INTERNAL_SERVER_ERROR).entity(Map("msg" -> error.getMessage)).build())
    }
  }
}
