package steve.client

import java.util.{Date, UUID}

import domain.Job
import org.mockito.Mockito._
import org.scalatest.FlatSpec
import org.scalatest.Matchers._
import utils.JsonUtils

import scalaj.http.{BaseHttp, HttpResponse}

class SteveClientSpec extends FlatSpec {
  val host = "http://localhost:9092"
  val mockHttp = mock(classOf[BaseHttp], RETURNS_DEEP_STUBS)
  val client = new SteveClient(mockHttp, host)

  "Client" should "create a new Job and return Job ID" in {
    val jobId = UUID.randomUUID
    val expectedResponse = Map("id" -> jobId.toString, "msg" -> "Created")
    val jsonResponse = JsonUtils.toJson(expectedResponse)
    val job = Job(id = jobId, appName = "cannonball", state = "START", createdAt = new Date(), updatedAt = None, attributes = Map("test" -> "test"))

    when {
      mockHttp(host + "/job")
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
    val expectedResponse = JsonUtils.toJson(dummyJob)

    when {
      mockHttp(host + "/job")
        .method("GET")
        .asString
    }.thenReturn(HttpResponse[String](expectedResponse, 201, Map()))

    val response = client.getJob(jobId)
    response should be(dummyJob)
  }
}
