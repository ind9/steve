package controller

import java.util.{Date, UUID}
import javax.ws.rs._
import javax.ws.rs.container.{AsyncResponse, Suspended}
import javax.ws.rs.core.Response.Status
import javax.ws.rs.core.{MediaType, Response}

import com.google.inject.Inject
import dao.Jobs
import domain.Job
import steve.SteveConfiguration

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

@Path("/job")
class JobController @Inject()(steveConfiguration: SteveConfiguration, jobs: Jobs, implicit val ec: ExecutionContext) {

  @PUT
  @Path("/")
  @Produces(Array(MediaType.APPLICATION_JSON))
  def createJob(job: Job, @Suspended res: AsyncResponse) = {
    val newJob: Job = job.copy(id = UUID.randomUUID, createdAt = new Date())
    jobs.insert(List(newJob)).onComplete {
      case Success(_) => res.resume(Response.status(Status.CREATED).entity(Map("id" -> newJob.id, "msg" -> "Created")).build())
      case Failure(_) => res.resume(Response.status(Status.INTERNAL_SERVER_ERROR).build())
    }
  }

  @GET
  @Path("/{id}")
  @Produces(Array(MediaType.APPLICATION_JSON))
  def getJob(@PathParam("id") jobId: UUID, @Suspended res: AsyncResponse) = {
    jobs.select(jobId).onComplete {
      case Success(Some(job)) => res.resume(Response.status(Status.OK).entity(job).build())
      case Success(None) => res.resume(Response.status(Status.NOT_FOUND).entity(Map("id" -> jobId, "msg" -> "Not Found")).build())
      case Failure(_) => res.resume(Response.status(Status.INTERNAL_SERVER_ERROR).build())
    }
  }

  @POST
  @Path("/{id}")
  @Produces(Array(MediaType.APPLICATION_JSON))
  def updateJob(@PathParam("id") jobId: UUID, job: Job, @Suspended res: AsyncResponse) = {
    val updatedJob: Job = job.copy(id = jobId, updatedAt = Some(new Date()))
    jobs.update(updatedJob).onComplete {
      case Success(_) => res.resume(Response.status(Status.OK).entity(updatedJob).build())
      case Failure(_) => res.resume(Response.status(Status.INTERNAL_SERVER_ERROR).build())
    }
  }

  @DELETE
  @Path("/{id}")
  @Produces(Array(MediaType.APPLICATION_JSON))
  def deleteJob(@PathParam("id") jobId: UUID, @Suspended res: AsyncResponse) = {
    jobs.delete(jobId).onComplete {
      case Success(rowsDeleted) if rowsDeleted == 0 => res.resume(Response.status(Status.NOT_FOUND).entity(Map("id" -> jobId, "msg" -> "Not Found")).build())
      case Success(rowsDeleted) => res.resume(Response.status(Status.OK).entity(Map("id" -> jobId, "msg" -> s"Deleted", "rowsAffected" -> rowsDeleted)).build())
      case Failure(_) => res.resume(Response.status(Status.INTERNAL_SERVER_ERROR).build())
    }
  }
}
