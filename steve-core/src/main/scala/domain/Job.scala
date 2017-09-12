package domain

import java.util.{Date, UUID}

case class Job(id: UUID,
               appName: String,
               status: String,
               createdAt: Date,
               updatedAt: Option[Date],
               attributes: Map[String,String])
