package domain

import java.util.Date

case class Item(id: String,
                jobId: Long,
                status: String,
                createdAt: Date,
                updatedAt: Option[Date],
                attributes: Map[String, String])
