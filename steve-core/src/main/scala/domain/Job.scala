package domain

import java.util.Date

import play.api.libs.json.JsValue

case class Job(id:Long,
               appName: String,
               status: String,
               createdAt: Date,
               updatedAt: Date,
               attributes: JsValue)
