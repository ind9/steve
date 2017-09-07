package controller

import java.util.Date
import javax.ws.rs.core.MediaType
import javax.ws.rs.{GET, Path, Produces}

import com.google.inject.Inject
import dao.Jobs
import domain.Job
import play.api.libs.json.JsObject
import steve.SteveConfiguration

@Path("/job")
class JobController @Inject()(steveConfiguration: SteveConfiguration, jobs: Jobs) {

  @GET
  @Path("/{id}")
  @Produces(Array(MediaType.APPLICATION_JSON))
  def getDetails() = {
    jobs.insert(List(Job(123,"test","test",new Date,new Date, JsObject.empty)))
    steveConfiguration
  }
}
