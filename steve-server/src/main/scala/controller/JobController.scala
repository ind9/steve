package controller

import java.util.Date
import javax.ws.rs.container.{AsyncResponse, Suspended}
import javax.ws.rs.core.Response
import javax.ws.rs.core.Response.Status
import javax.ws.rs.core.MediaType
import javax.ws.rs.{GET, Path, Produces}

import com.google.inject.Inject
import dao.Jobs
import domain.Job
import play.api.libs.json.JsObject
import steve.SteveConfiguration

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

@Path("/job")
class JobController @Inject()(steveConfiguration: SteveConfiguration, jobs: Jobs, implicit val ec: ExecutionContext) {

  @GET
  @Path("/{id}")
  @Produces(Array(MediaType.APPLICATION_JSON))
  def getDetails(@Suspended res: AsyncResponse) = {
    jobs.insert(List(Job(123,"test","test",new Date,new Date, JsObject.empty))).onComplete {
      case Success(_) => res.resume(Response.status(Status.CREATED).build())
      case Failure(_) => res.resume(Response.status(Status.INTERNAL_SERVER_ERROR).build())
    }
  }
}
