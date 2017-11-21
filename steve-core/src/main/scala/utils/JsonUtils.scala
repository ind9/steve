package utils

import java.util.Date

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule

import scala.reflect.ClassTag

object JsonUtils {
  def fromJson[T: ClassTag](input: String): T = {
    val clazzTag = implicitly[ClassTag[T]]
    new ObjectMapper()
      .registerModule(DefaultScalaModule)
      .readValue(input, clazzTag.runtimeClass).asInstanceOf[T]
  }

  def toJson[T](input: T): String = {
    new ObjectMapper()
      .registerModule(DefaultScalaModule)
      .writeValueAsString(input)
  }
}

object DateConverter {

  implicit class OptionalDateFromString(dateString: Option[String]) {
    def toDate: Option[Date] = dateString match {
      case Some(date) => Some(date.toDate)
      case None => None
    }
  }

  implicit class DateFromString(str: String) {
    def toDate: Date = new Date(str.toLong)
  }

}
