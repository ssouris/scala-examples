package com.ssouris

import akka.http.caching.scaladsl.CachingSettings
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.{ContentTypes, StatusCodes}
import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
import akka.testkit.TestDuration
import com.typesafe.config.Config
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.duration._

@RunWith(classOf[JUnitRunner])
class ApiIntegrationTests extends FlatSpec with Matchers with ScalatestRouteTest with ExchangeRateService {

  override def config: Config = testConfig

  override val defaultCachingSettings: CachingSettings = CachingSettings(system)

  implicit val timeout: RouteTestTimeout = RouteTestTimeout(5.seconds.dilated)

  val request1 = ExchangeRateRequest("EUR", "GBP", 100)
  val request2 = ExchangeRateRequest("GBP", "EUR", 100)
  val response2 = ExchangeRateResponse(1.1381, 1.1608619999999998, 1.1381)

  it should s"respond to convert request from EUR to GBP $request1" in {
    Post(s"/api/convert", request1) ~> routes ~> check {
      status shouldBe StatusCodes.OK
      contentType shouldBe ContentTypes.`application/json`

      responseAs[ExchangeRateResponse] should matchPattern({
        case ExchangeRateResponse(_, _, request1.amount) =>
      })
    }
  }

  it should s"respond to convert request from GBP to EUR $request2" in {
    Post(s"/api/convert", request2) ~> routes ~> check {
      status shouldBe StatusCodes.OK
      contentType shouldBe ContentTypes.`application/json`
      responseAs[ExchangeRateResponse] should matchPattern({
        case ExchangeRateResponse(_, _, request2.amount) =>
      })
    }
  }

  it should s"respond with BadRequest when base currency is empty" in {
    Post(s"/api/convert", ExchangeRateRequest("", "EUR", 100)) ~> routes ~> check {
      status shouldBe StatusCodes.BadRequest
      contentType shouldBe ContentTypes.`application/json`
      responseAs[String] shouldBe "To Currency 'EUR' not supported"
    }
  }

  it should s"respond with BadRequest when external API responds with BadRequest" in {
    val request = ExchangeRateRequest("EUR", "XOR", 100)
    Post(s"/api/convert", request) ~> routes ~> check {
      status shouldBe StatusCodes.BadRequest
      contentType shouldBe ContentTypes.`application/json`
      responseAs[String] shouldBe s"To Currency '${request.toCurrency}' not supported"
    }
  }

}
