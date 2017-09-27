package steve.client

import domain.{Item, Job}
import utils.JsonUtils

import scalaj.http.BaseHttp

case class ItemBatch(items: List[Map[String, Any]]) {
  def +(another: ItemBatch) = ItemBatch(items ++ another.items)
}

object ItemBatch {
  def apply(jobId: String, status: String, attributes: Map[String,String]): ItemBatch = {
    ItemBatch(List(Map[String,Any]("jobId" -> jobId, "status" -> status, "attributes" -> attributes)))
  }

}

class SteveClient(httpClient: BaseHttp, host: String) {
  val connectionTimeoutInMillis = 10000
  val readTimeoutInMillis = 30000

  def addJob(appName: String, state: String, attributes: Map[String,String]): Option[String] = {
    val jsonInput = Map[String,Any]("appName" -> appName, "state" -> state, "attributes" -> attributes)
    val data = JsonUtils.toJson(jsonInput)
    val response = httpClient(s"$host/job")
      .postData(data)
      .header("content-type", "application/json")
      .method("PUT")
      .timeout(connTimeoutMs = connectionTimeoutInMillis,readTimeoutMs = readTimeoutInMillis)
      .asString
    val jobInfo = JsonUtils.fromJson[Map[String, String]](response.body)
    jobInfo.get("id")
  }

  def getJob(jobId: String): Job = {
    val response = httpClient(s"$host/job/$jobId")
      .method("GET")
      .timeout(connTimeoutMs = connectionTimeoutInMillis,readTimeoutMs = readTimeoutInMillis)
      .asString
    val jobInfo = JsonUtils.fromJson[Job](response.body)
    jobInfo
  }

  def updateJobState(jobId: String, state: String): Job = {
    //TODO - Change the below to in-place updates
    val job = getJob(jobId)
    val data = JsonUtils.toJson(job.copy(state = state))
    val response = httpClient(s"$host/job/${jobId.toString}")
      .postData(data)
      .header("content-type", "application/json")
      .method("POST")
      .timeout(connTimeoutMs = connectionTimeoutInMillis,readTimeoutMs = readTimeoutInMillis)
      .asString
    val jobInfo = JsonUtils.fromJson[Job](response.body)
    jobInfo
  }

  def deleteJob(jobId: String): Option[String] = {
    val response = httpClient(s"$host/job/$jobId")
      .method("DELETE")
      .timeout(connTimeoutMs = connectionTimeoutInMillis,readTimeoutMs = readTimeoutInMillis)
      .asString
    val jobInfo = JsonUtils.fromJson[Map[String, String]](response.body)
    jobInfo.get("rowsAffected")
  }

  def getJobStats(jobId: String): Map[String, Int] = {
    val response = httpClient(s"$host/job/$jobId/stats")
      .method("GET")
      .timeout(connTimeoutMs = connectionTimeoutInMillis,readTimeoutMs = readTimeoutInMillis)
      .asString
    val jobStats = JsonUtils.fromJson[Map[String, Int]](response.body)
    jobStats
  }

  def addItem(jobId: String, status: String, attributes: Map[String,String]): Option[String] = {
    val jsonInput = Map[String,Any]("jobId" -> jobId, "status" -> status, "attributes" -> attributes)
    val data = JsonUtils.toJson(jsonInput)
    val response = httpClient(s"$host/item")
      .postData(data)
      .header("content-type", "application/json")
      .method("PUT")
      .timeout(connTimeoutMs = connectionTimeoutInMillis,readTimeoutMs = readTimeoutInMillis)
      .asString
    val itemInfo = JsonUtils.fromJson[Map[String, String]](response.body)
    itemInfo.get("id")
  }

  def addItems(batch: ItemBatch): Boolean = {
    val data = JsonUtils.toJson(batch.items)
    val response = httpClient(s"$host/item/bulk")
      .postData(data)
      .header("content-type", "application/json")
      .method("PUT")
      .timeout(connTimeoutMs = connectionTimeoutInMillis,readTimeoutMs = readTimeoutInMillis)
      .asString
    response.is2xx
  }

  def getItem(itemId: String): Item = {
    val response = httpClient(s"$host/item/$itemId")
      .method("GET")
      .timeout(connTimeoutMs = connectionTimeoutInMillis,readTimeoutMs = readTimeoutInMillis)
      .asString
    val itemInfo = JsonUtils.fromJson[Item](response.body)
    itemInfo
  }

  def updateItemStatus(itemId: String, status: String): Item = {
    //TODO - Change the below to in-place updates
    val item = getItem(itemId)
    val data = JsonUtils.toJson(item.copy(status = status))
    val response = httpClient(s"$host/item/$itemId")
      .postData(data)
      .header("content-type", "application/json")
      .method("POST")
      .timeout(connTimeoutMs = connectionTimeoutInMillis,readTimeoutMs = readTimeoutInMillis)
      .asString
    val itemInfo = JsonUtils.fromJson[Item](response.body)
    itemInfo
  }

  def deleteItem(itemId: String): Option[String] = {
    val response = httpClient(s"$host/item/$itemId")
      .method("DELETE")
      .timeout(connTimeoutMs = connectionTimeoutInMillis,readTimeoutMs = readTimeoutInMillis)
      .asString
    val itemInfo = JsonUtils.fromJson[Map[String, String]](response.body)
    itemInfo.get("rowsAffected")
  }
}
