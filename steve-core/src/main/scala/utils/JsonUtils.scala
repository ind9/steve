package utils

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
