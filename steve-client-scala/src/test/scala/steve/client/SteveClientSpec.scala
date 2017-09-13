package steve.client

import java.util.{Date, UUID}

import domain.Job
import org.mockito.Mockito._
import org.scalatest.FlatSpec
import org.scalatest.Matchers._
import utils.JsonUtils

import scalaj.http.{BaseHttp, HttpResponse}

class SteveClientSpec extends FlatSpec {
  val host = "http://localhost:9092" // TODO - move this to config?
  val mockHttp = mock(classOf[BaseHttp], RETURNS_DEEP_STUBS)
  val client = new SteveClient(mockHttp, host)

  "Client" should "create a new Job and return Job ID" in {
    val jobId = UUID.randomUUID
    val expectedResponse = Map("id" -> jobId.toString, "msg" -> "Created")
    val jsonResponse = JsonUtils.toJson(expectedResponse)
    val job = Job(id = jobId, appName = "cannonball", state = "START", createdAt = new Date(), updatedAt = None, attributes = Map("test" -> "test"))

    when {
      mockHttp(s"${host}/job")
        .postData(JsonUtils.toJson(job))
        .method("PUT")
        .asString
    }.thenReturn(HttpResponse[String](jsonResponse, 201, Map()))

    val response = client.addJob(job)
    response should be(expectedResponse.get("id"))
  }

  "Client" should "get a Job's details for a given Job ID" in {
    val jobId = UUID.randomUUID
    val dummyJob = Job(id = jobId, appName = "cannonball", state = "START", createdAt = new Date(), updatedAt = None, attributes = Map("test" -> "test"))
    val dummyResponse = JsonUtils.toJson(dummyJob)

    when {
      mockHttp(s"${host}/job/${jobId.toString}")
        .method("GET")
        .asString
    }.thenReturn(HttpResponse[String](dummyResponse, 200, Map()))

    val response = client.getJob(jobId)
    response should be(dummyJob)
  }

  "Client" should "update a Job's details" in {
    val jobId = UUID.randomUUID
    val dummyJob = Job(id = jobId, appName = "cannonball", state = "UPDATING", createdAt = new Date(), updatedAt = None, attributes = Map("test" -> "test"))
    val dummyResponse = JsonUtils.toJson(dummyJob)

    when {
      mockHttp(s"${host}/job/${jobId.toString}")
        .postData(JsonUtils.toJson(dummyJob))
        .method("POST")
        .asString
    }.thenReturn(HttpResponse[String](dummyResponse, 200, Map()))

    val response = client.updateJob(jobId, dummyJob)
    response should be(dummyJob)
  }

  "Client" should "get a delete a Job, given a Job ID" in {
    val jobId = UUID.randomUUID
    val expectedResponse = Map("id" -> jobId.toString, "msg" -> "Deleted", "rowsAffected" -> "1")
    val jsonResponse = JsonUtils.toJson(expectedResponse)

    when {
      mockHttp(s"${host}/job/${jobId.toString}")
        .method("DELETE")
        .asString
    }.thenReturn(HttpResponse[String](jsonResponse, 200, Map()))

    val response = client.deleteJob(jobId)
    response should be(expectedResponse.get("rowsAffected"))
  }
}
