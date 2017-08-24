package controller

import javax.ws.rs.core.{MediaType, Response}
import javax.ws.rs.{GET, Path, Produces}

import com.google.inject.Inject
import steve.SteveConfiguration

@Path("/job")
class JobController @Inject()(steveConfiguration: SteveConfiguration) {

  @GET
  @Path("/{id}")
  @Produces(Array(MediaType.APPLICATION_JSON))
  def getDetails() = {
    steveConfiguration
  }
}
