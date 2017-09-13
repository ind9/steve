package steve.client

import domain.Job
import utils.JsonUtils

import scalaj.http.BaseHttp


class SteveClient(httpClient: BaseHttp, host: String) {
  def addJob(job: Job): Option[String] = {
    val data = JsonUtils.toJson(job)
    val response = httpClient(host + "/job")
      .postData(data)
      .method("PUT")
      .asString
    val jobInfo = JsonUtils.fromJson[Map[String, String]](response.body)
    jobInfo.get("id")
  }
}
