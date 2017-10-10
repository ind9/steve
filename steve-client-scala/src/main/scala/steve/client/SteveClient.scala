package steve.client

import domain.{Item, Job}
import utils.JsonUtils

import scalaj.http.BaseHttp

case class ClientException(private val message: String = "",
                           private val cause: Throwable = None.orNull)
  extends Exception(message, cause)
case class ItemBatch(items: List[Map[String, Any]]) {
  def +(another: ItemBatch) = ItemBatch(items ++ another.items)
}

object ItemBatch {
  def apply(jobId: String, status: String, attributes: Map[String,String]): ItemBatch = {
    ItemBatch(List(Map[String,Any]("jobId" -> jobId, "status" -> status, "attributes" -> attributes)))
  }

}

//TODO: Handle retries in the client logic
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

  def getJobIdsByState(state: String) = {
    val response = httpClient(s"$host/job?state=$state")
      .method("GET")
      .timeout(connTimeoutMs = connectionTimeoutInMillis,readTimeoutMs = readTimeoutInMillis)
      .asString
    JsonUtils.fromJson[List[String]](response.body)
  }

  def updateJobState(jobId: String, state: String): Boolean = {
    val response = httpClient(s"$host/job/${jobId.toString}/state")
      .postData(state)
      .header("content-type", "application/json")
      .method("POST")
      .timeout(connTimeoutMs = connectionTimeoutInMillis,readTimeoutMs = readTimeoutInMillis)
      .asString
    response.is2xx
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

  private def checkItemByStatus(qpString: String) = {
    val response = httpClient(s"$host/item/status?$qpString")
      .method("HEAD")
      .timeout(connTimeoutMs = connectionTimeoutInMillis,readTimeoutMs = readTimeoutInMillis)
      .asString
    if(response.is2xx)
      true
    else if(response.is4xx)
      false
    else
      throw ClientException(response.toString)
  }

  def checkIfItemsWithStatus(jobId: String, status: String) = {
    checkItemByStatus(s"jobId=$jobId&status=$status")
  }

  def checkIfItemsWithoutStatus(jobId: String, status: String) = {
    //NOTE: There's a negation(`!`) operator on the status value
    checkItemByStatus(s"jobId=$jobId&status=!$status")
  }

  def updateItemStatus(itemId: String, status: String): Boolean = {
    val response = httpClient(s"$host/item/$itemId/status")
      .postData(status)
      .header("content-type", "application/json")
      .method("POST")
      .timeout(connTimeoutMs = connectionTimeoutInMillis,readTimeoutMs = readTimeoutInMillis)
      .asString
    response.is2xx
  }

  def updateItemStatus(queryAttrs: Map[String, String], queryStatus: Option[String], updateStatus:String): Boolean = {
    val qsMap = queryStatus.map(status => Map("status" -> status)).getOrElse(Map()) ++ queryAttrs
    val queryString = qsMap.map{
      case(key,value) => key + "=" + value
    }.mkString("&")
    val response = httpClient(s"$host/item/status?$queryString")
      .postData(updateStatus)
      .header("content-type", "application/json")
      .method("POST")
      .timeout(connTimeoutMs = connectionTimeoutInMillis,readTimeoutMs = readTimeoutInMillis)
      .asString
    response.is2xx
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
