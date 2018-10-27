package com.ssouris

import java.io.InputStream

import com.fasterxml.jackson.databind.node._
import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper

import scala.collection.JavaConverters._


object JsonParser {

  val mapper: ObjectMapper with ScalaObjectMapper =
    new ObjectMapper() with ScalaObjectMapper {
      registerModule(DefaultScalaModule)
    }

  def jsonToMap(jsonStream: InputStream): Map[String, Any] =
    jsonToMap(Option(jsonStream).map(mapper.readValue[JsonNode](_)).orNull)

  def jsonToMap(value: JsonNode): Map[String, Any] = value match {
    case null | _: NullNode | _: MissingNode => Map()
    case _: ArrayNode => value.elements().asScala.zipWithIndex.map(no => no._2.toString -> getJsonValue(no._1)).toMap
    case x if !x.isObject => throw new IllegalArgumentException
    case _ => value.fields().asScala.map(b => b.getKey -> getJsonValue(b.getValue)).toMap
  }

  private def getJsonValue(jsonNode: JsonNode): Any = jsonNode match {
      case e: BooleanNode => e.booleanValue()
      case n: NumericNode => n.numberValue()
      case t: TextNode => t.textValue()
      case a: ArrayNode => a.elements().asScala.map(getJsonValue).toList
      case o: ObjectNode => jsonToMap(o)
      case p: POJONode => p.getPojo
      case b: BinaryNode => b.textValue()
      case _: NullNode | _: MissingNode | _ => Option.empty
    }

}
