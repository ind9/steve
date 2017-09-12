package domain

import java.util.{Date, UUID}

case class Item(id: UUID,
                jobId: UUID,
                status: String,
                createdAt: Date,
                updatedAt: Option[Date],
                attributes: Map[String, String])
