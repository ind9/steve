package steve.client

import java.util.UUID

import domain.{Item, Job}
import utils.JsonUtils

import scalaj.http.BaseHttp


class SteveClient(httpClient: BaseHttp, host: String) {
  def addJob(job: Job): Option[String] = {
    val data = JsonUtils.toJson(job)
    val response = httpClient(s"${host}/job")
      .postData(data)
      .method("PUT")
      .asString
    val jobInfo = JsonUtils.fromJson[Map[String, String]](response.body)
    jobInfo.get("id")
  }

  def getJob(jobId: UUID): Job = {
    val response = httpClient(s"${host}/job/${jobId.toString}")
      .method("GET")
      .asString
    val jobInfo = JsonUtils.fromJson[Job](response.body)
    jobInfo
  }

  def updateJob(jobId: UUID, updatedJob: Job): Job = {
    val data = JsonUtils.toJson(updatedJob)
    val response = httpClient(s"${host}/job/${jobId.toString}")
      .postData(data)
      .method("POST")
      .asString
    val jobInfo = JsonUtils.fromJson[Job](response.body)
    jobInfo
  }

  def deleteJob(jobId: UUID): Option[String] = {
    val response = httpClient(s"${host}/job/${jobId.toString}")
      .method("DELETE")
      .asString
    val jobInfo = JsonUtils.fromJson[Map[String, String]](response.body)
    jobInfo.get("rowsAffected")
  }

  def addItem(item: Item): Option[String] = {
    val data = JsonUtils.toJson(item)
    val response = httpClient(s"${host}/item")
      .postData(data)
      .method("PUT")
      .asString
    val itemInfo = JsonUtils.fromJson[Map[String, String]](response.body)
    itemInfo.get("id")
  }

  def getItem(itemId: UUID): Item = {
    val response = httpClient(s"${host}/item/${itemId.toString}")
      .method("GET")
      .asString
    val itemInfo = JsonUtils.fromJson[Item](response.body)
    itemInfo
  }

  def updateItem(itemId: UUID, updatedItem: Item): Item = {
    val data = JsonUtils.toJson(updatedItem)
    val response = httpClient(s"${host}/item/${itemId.toString}")
      .postData(data)
      .method("POST")
      .asString
    val itemInfo = JsonUtils.fromJson[Item](response.body)
    itemInfo
  }

  def deleteItem(itemId: UUID): Option[String] = {
    val response = httpClient(s"${host}/item/${itemId.toString}")
      .method("DELETE")
      .asString
    val itemInfo = JsonUtils.fromJson[Map[String, String]](response.body)
    itemInfo.get("rowsAffected")
  }
}
