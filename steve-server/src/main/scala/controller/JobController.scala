package controller

import java.util.Date
import javax.ws.rs.container.{AsyncResponse, Suspended}
import javax.ws.rs.core.Response
import javax.ws.rs.core.Response.Status
import javax.ws.rs.core.MediaType
import javax.ws.rs.{GET, PUT, Path, PathParam, Produces}

import com.google.inject.Inject
import dao.Jobs
import domain.Job
import play.api.libs.json.JsObject
import steve.SteveConfiguration

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

@Path("/job")
class JobController @Inject()(steveConfiguration: SteveConfiguration, jobs: Jobs, implicit val ec: ExecutionContext) {

  @PUT
  @Path("/{id}")
  @Produces(Array(MediaType.APPLICATION_JSON))
  def putDetails(@Suspended res: AsyncResponse) = {
    // TODO - get all job details from PUT request body.
    jobs.insert(List(Job(123,"test","test",new Date,new Date, JsObject.empty))).onComplete {
      case Success(_) => res.resume(Response.status(Status.CREATED).build())
      case Failure(_) => res.resume(Response.status(Status.INTERNAL_SERVER_ERROR).build())
    }
  }

  @GET
  @Path("/{id}")
  @Produces(Array(MediaType.APPLICATION_JSON))
  def getDetails(@PathParam("id") jobId: Long, @Suspended res: AsyncResponse) = {
    jobs.select(jobId).onComplete {
      case Success(Some(job)) => res.resume(Response.status(Status.OK).entity(job).build())
      case Success(None) => res.resume(Response.status(Status.NOT_FOUND).entity(Map("id" -> jobId, "msg" -> "Not Found")).build())
      case Failure(_) => res.resume(Response.status(Status.INTERNAL_SERVER_ERROR).build())
    }
  }
}
