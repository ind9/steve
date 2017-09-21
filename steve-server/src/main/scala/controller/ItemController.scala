package controller

import java.util.{Date, UUID}
import javax.ws.rs._
import javax.ws.rs.container.{AsyncResponse, Suspended}
import javax.ws.rs.core.Response.Status
import javax.ws.rs.core.{MediaType, Response}

import com.google.inject.Inject
import dao.Items
import domain.Item
import steve.SteveConfiguration

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

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
