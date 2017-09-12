package domain

import java.util.{Date, UUID}

case class Job(id: UUID,
               appName: String,
               state: String,
               createdAt: Date,
               updatedAt: Option[Date],
               attributes: Map[String, String])
