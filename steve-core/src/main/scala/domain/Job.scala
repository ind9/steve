package domain

import java.util.Date

case class Job(id: Long,
               appName: String,
               status: String,
               createdAt: Date,
               updatedAt: Option[Date],
               attributes: Map[String,String])
