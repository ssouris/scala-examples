package com.ssouris

import java.io.{ByteArrayInputStream, InputStream}
import java.math.BigInteger
import java.nio.charset.Charset._

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.{ArrayNode, JsonNodeFactory, MissingNode, ObjectNode}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import com.ssouris.JsonParser.jsonToMap
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class JsonParserTest extends FunSuite {

  val mapper: ObjectMapper with ScalaObjectMapper =
    new ObjectMapper() with ScalaObjectMapper {
      registerModule(DefaultScalaModule)
    }

  test("test all json types in a json object") {
    val value =
      """
        |{
        | "a" : true,
        | "b" : false,
        | "c" : 1,
        | "d" : 1.123,
        | "e" : [1.123, 1.23, 12.32],
        | "f" : [[1, 2, 3, ["a", "b", "c"]], 1.23, 12.32]
        |}
      """.stripMargin
    assert(jsonToMap(value.toInputStream) === value.toMapUsingJackson)
  }

  test("test simple object") {
    val value =
      """
        |{
        | "rtt" : 1.23,
        | "mtt" : 12324,
        | "sos": "King's Cross",
        | "scrape":true
        |}
      """.stripMargin
    assert(jsonToMap(value.toInputStream) === value.toMapUsingJackson)
  }

  test("test simple nested object") {
    val value =
      """
        |{
        | "rtt" : 1.23,
        | "mtt" : 12324,
        | "address": {
        |   "street" : "King's Cross",
        |   "postCode" : "N4",
        |   "nearbyAmazonStores" : [  "A1", "A2", "A3" ]
        | },
        | "scrape":true
        |}
        |
      """.stripMargin
    assert(jsonToMap(value.toInputStream) === value.toMapUsingJackson)
  }

  test("test null") {
    val inputStream: InputStream = null
    jsonToMap(inputStream) === Map()
  }

  test("test empty string") {
    assert(jsonToMap(MissingNode.getInstance()) == Map())
  }

  test("test empty json object") {
    assert(jsonToMap("{}".toInputStream) === Map())
  }

  test("test json array") {
    val value = "[1, 2, 3, 4]"
    jsonToMap(value.toInputStream) === Map("0" -> 1, "1" -> 2, "2" -> 3, "3" -> 4)
  }

  test("test json array with complex elements") {
    val value =
      """
        |[
        | 1,
        | { "testA" : 1, "testB": 2 },
        | [ 3 , "ST", { "p":"P" } ],
        | "Stathis",
        | null,
        | { "testNullField" : null },
        | [{ "testNullField" : null }, null, null]
        |]
      """.stripMargin
    jsonToMap(value.toInputStream) ===
      Map(
        "0" -> 1,
        "1" -> Map(
          "testA" -> 1,
          "testB" -> 2
        ),
        "2" -> List(
          3,
          "ST",
          Map("p" -> "P")
        ),
        "3" -> "Stathis",
        "4" -> Option.empty,
        "5" -> Map("testNullField" -> Option.empty),
        "6" -> List(Map("testNullField" -> Option.empty), Option.empty, Option.empty)
      )
  }

  test("test array json node") {
    jsonToMap(new ArrayNode(JsonNodeFactory.instance)) === Map()
  }


  test("test BigDecimal and BigInteger nodes") {
    val objectNode = new ObjectNode(JsonNodeFactory.instance)
    val decimal = new java.math.BigDecimal("123123123123123.123123123123123")
    val integer = new BigInteger("123123123123123")
    objectNode.put("bigDecimal", decimal)
    objectNode.put("bigInteger", integer)
    assert(jsonToMap(objectNode) === mapper.convertValue[Map[String, Any]](objectNode))
  }

  test("test MissingNode inside ArrayNode") {
    val objectNode = new ObjectNode(JsonNodeFactory.instance)
    val arrayNode = new ArrayNode(JsonNodeFactory.instance)
    arrayNode.add(12.2)
    arrayNode.add(MissingNode.getInstance())
    objectNode.replace("arrayNode", arrayNode)
    assert(jsonToMap(objectNode) === Map("arrayNode" -> List(12.2, Option.empty)))
  }

  test("test simple json") {
    val value =
      """
        |{
        |  "connect" : 200,
        |  "dns" : 33,
        |  "firstByte" : 862,
        |  "receive" : 0,
        |  "send" : 0,
        |  "ssl" : 431,
        |  "total" : 862,
        |  "wait" : 198
        |}
      """.stripMargin
    assert(jsonToMap(value.toInputStream) === value.toMapUsingJackson)
  }

  test("test json with nested array") {
    val value =
      """
        |{
        |  "connect" : [200, 100, 123],
        |  "dns" : 33,
        |  "firstByte" : 862,
        |  "receive" : 0,
        |  "send" : 0,
        |  "ssl" : 431,
        |  "total" : 862,
        |  "wait" : 198
        |}
      """.stripMargin
    assert(jsonToMap(value.toInputStream) === value.toMapUsingJackson)
  }

  test("json with 'datetime' fields") {
    val value =
      """{
        |  "b": "2000-12-25T05:59:59.999Z",
        |  "c": [
        |    { "ts": "2002-12-25T05:59:59.999+00:00"},
        |    { "ts": "2002-12-25T05:59:59.999+00:00",  "nts": "2002-12-25T05:59:59.999+00:00"},
        |    { "nts": "2002-12-25T05:59:59.999+00:00",  "ts": "2002-12-25T05:59:59.999+00:00"}
        |  ],
        |  "d": "2005-12-25T05:59:59.999Z",
        |  "e": [
        |    { "m": { "nts": "2002-12-25T05:59:59.999+00:00" } },
        |    { "n": [
        |      { "x": { "nts": "2002-12-25T05:59:59.999+00:00", "ts": "2002-12-25T05:59:59.999+00:00"}},
        |      { "nts": "2002-12-25T05:59:59.999+00:00", "nts2": "2002-12-25T05:59:59.999+00:00"}
        |    ]},
        |    { "o": { "nts": "2002-12-25T05:59:59.999+00:00" } }
        |  ]
        |}""".stripMargin

    assert(jsonToMap(value.toInputStream) === value.toMapUsingJackson)
  }

  // Extension functions for String class
  implicit class StringUtils(value: String) {

    def toInputStream: InputStream =
      Option(value).map(b => new ByteArrayInputStream(b.getBytes(forName("UTF-8")))).orNull

    def toMapUsingJackson: Map[String, Any] =
      Option(value).map(v => mapper.readValue[Map[String, Any]](v)).orNull



  }

}
