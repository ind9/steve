package domain

import java.util.Date

case class Job(id: Long,
               appName: String,
               state: String,
               createdAt: Date,
               updatedAt: Option[Date],
               attributes: Map[String, String])
