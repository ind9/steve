package steve.client

import java.util.UUID

import domain.Job
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
}
