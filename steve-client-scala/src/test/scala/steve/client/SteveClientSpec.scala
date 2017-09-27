package steve.client

import java.util.{Date, UUID}
import org.mockito.Matchers._
import domain.{Item, Job}
import org.mockito.Mockito._
import org.scalatest.FlatSpec
import org.scalatest.Matchers._
import utils.JsonUtils

import scalaj.http.{BaseHttp, HttpResponse}

class SteveClientSpec extends FlatSpec {
  val host = "http://localhost:9092" // TODO - move this to config?
  val mockHttp: BaseHttp = mock(classOf[BaseHttp], RETURNS_DEEP_STUBS)
  val client = new SteveClient(mockHttp, host)

  "Client" should "create a new Job and return Job ID" in {
    val jobId = UUID.randomUUID
    val expectedResponse = Map("id" -> jobId.toString, "msg" -> "Created")
    val jsonResponse = JsonUtils.toJson(expectedResponse)
    val job = Map[String,Any]("appName" -> "cannonball", "state" -> "START", "attributes" -> Map("test" -> "test"))
    when {
      mockHttp(s"$host/job")
        .postData(JsonUtils.toJson(job))
        .header("content-type", "application/json")
        .method("PUT")
        .timeout(anyInt(),anyInt())
        .asString
    }.thenReturn(HttpResponse[String](jsonResponse, 201, Map()))

    val response = client.addJob("cannonball", "START", Map("test" -> "test"))
    response should be(expectedResponse.get("id"))
  }

  it should "get a Job's details for a given Job ID" in {
    val jobId = UUID.randomUUID
    val dummyJob = Job(id = jobId, appName = "cannonball", state = "START", createdAt = new Date(), updatedAt = None, attributes = Map("test" -> "test"))
    val dummyResponse = JsonUtils.toJson(dummyJob)

    when {
      mockHttp(s"$host/job/${jobId.toString}")
        .method("GET")
        .timeout(anyInt(),anyInt())
        .asString
    }.thenReturn(HttpResponse[String](dummyResponse, 200, Map()))

    val response = client.getJob(jobId.toString)
    response should be(dummyJob)
  }

  it should "update a Job's state" in {
    val jobId = UUID.randomUUID
    val dummyJobForGet = Job(id = jobId, appName = "cannonball", state = "START", createdAt = new Date(), updatedAt = None, attributes = Map("test" -> "test"))
    val dummyJobForPost = dummyJobForGet.copy(state = "UPDATE")
    val dummyResponseForGet = JsonUtils.toJson(dummyJobForGet)
    val dummyResponseForPost = JsonUtils.toJson(dummyJobForPost)

    when {
      mockHttp(s"$host/job/${jobId.toString}")
        .method("GET")
        .timeout(anyInt(),anyInt())
        .asString
    }.thenReturn(HttpResponse[String](dummyResponseForGet, 200, Map()))

    when {
      mockHttp(s"$host/job/${jobId.toString}")
        .postData(JsonUtils.toJson(dummyJobForPost))
        .header("content-type", "application/json")
        .method("POST")
        .timeout(anyInt(),anyInt())
        .asString
    }.thenReturn(HttpResponse[String](dummyResponseForPost, 200, Map()))

    val response = client.updateJobState(jobId.toString, "UPDATE")
    response should be(dummyJobForPost)
  }

  it should "get a delete a Job, given a Job ID" in {
    val jobId = UUID.randomUUID
    val expectedResponse = Map("id" -> jobId.toString, "msg" -> "Deleted", "rowsAffected" -> "1")
    val jsonResponse = JsonUtils.toJson(expectedResponse)

    when {
      mockHttp(s"$host/job/${jobId.toString}")
        .method("DELETE")
        .timeout(anyInt(),anyInt())
        .asString
    }.thenReturn(HttpResponse[String](jsonResponse, 200, Map()))

    val response = client.deleteJob(jobId.toString)
    response should be(expectedResponse.get("rowsAffected"))
  }

  it should "get a item status distribution for a given Job ID" in {
    val jobId = UUID.randomUUID
    val dummyStats = Map("CRASHED" -> 1, "FINISHED" -> 7, "IN_PROGRESS" -> 2)
    val dummyResponse = JsonUtils.toJson(dummyStats)

    when {
      mockHttp(s"$host/job/${jobId.toString}/stats")
        .method("GET")
        .timeout(anyInt(),anyInt())
        .asString
    }.thenReturn(HttpResponse[String](dummyResponse, 200, Map()))

    val response = client.getJobStats(jobId.toString)
    response should be(dummyStats)
  }

  it should "create a new Item and return Item ID" in {
    val itemId = UUID.randomUUID
    val jobId = UUID.randomUUID
    val expectedResponse = Map("id" -> itemId.toString, "msg" -> "Created")
    val jsonResponse = JsonUtils.toJson(expectedResponse)
    val item = Map[String,Any]("jobId" -> jobId.toString, "status" -> "NEW", "attributes" -> Map("test" -> "test"))

    when {
      mockHttp(s"$host/item")
        .postData(JsonUtils.toJson(item))
        .header("content-type", "application/json")
        .method("PUT")
        .timeout(anyInt(),anyInt())
        .asString
    }.thenReturn(HttpResponse[String](jsonResponse, 201, Map()))

    val response = client.addItem(jobId.toString, "NEW", Map("test" -> "test"))
    response should be(expectedResponse.get("id"))
  }

  it should "get a Item's details for a given Item ID" in {
    val itemId = UUID.randomUUID
    val jobId = UUID.randomUUID
    val dummyItem = Item(id = itemId, jobId = jobId, status = "START", createdAt = new Date(), updatedAt = None, attributes = Map("test" -> "test"))
    val dummyResponse = JsonUtils.toJson(dummyItem)

    when {
      mockHttp(s"$host/item/${itemId.toString}")
        .method("GET")
        .timeout(anyInt(),anyInt())
        .asString
    }.thenReturn(HttpResponse[String](dummyResponse, 200, Map()))

    val response = client.getItem(itemId.toString)
    response should be(dummyItem)
  }

  it should "update a Item's status" in {
    val itemId = UUID.randomUUID
    val jobId = UUID.randomUUID
    val dummyItemForGet = Item(id = itemId, jobId = jobId, status = "NEW", createdAt = new Date(), updatedAt = None, attributes = Map("test" -> "test"))
    val dummyItemForPost = dummyItemForGet.copy(status = "UPDATE")
    val dummyResponseForGet = JsonUtils.toJson(dummyItemForGet)
    val dummyResponseForPost = JsonUtils.toJson(dummyItemForPost)

    when {
      mockHttp(s"$host/item/${itemId.toString}")
        .method("GET")
        .timeout(anyInt(),anyInt())
        .asString
    }.thenReturn(HttpResponse[String](dummyResponseForGet, 200, Map()))

    when {
      mockHttp(s"$host/item/${itemId.toString}")
        .postData(JsonUtils.toJson(dummyItemForPost))
        .header("content-type", "application/json")
        .method("POST")
        .timeout(anyInt(),anyInt())
        .asString
    }.thenReturn(HttpResponse[String](dummyResponseForPost, 200, Map()))

    val response = client.updateItemStatus(itemId.toString, "UPDATE")
    response should be(dummyItemForPost)
  }

  it should "get a delete a Item, given an Item ID" in {
    val itemId = UUID.randomUUID
    val expectedResponse = Map("id" -> itemId.toString, "msg" -> "Deleted", "rowsAffected" -> "1")
    val jsonResponse = JsonUtils.toJson(expectedResponse)

    when {
      mockHttp(s"$host/item/${itemId.toString}")
        .method("DELETE")
        .timeout(anyInt(),anyInt())
        .asString
    }.thenReturn(HttpResponse[String](jsonResponse, 200, Map()))

    val response = client.deleteItem(itemId.toString)
    response should be(expectedResponse.get("rowsAffected"))
  }
}
