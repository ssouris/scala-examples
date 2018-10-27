package com.ssouris

import akka.NotUsed
import akka.http.caching.scaladsl.CachingSettings
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.{ContentTypes, HttpRequest, HttpResponse, StatusCodes}
import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
import akka.stream.scaladsl.Flow
import akka.testkit.TestDuration
import com.typesafe.config.Config
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.duration._

@RunWith(classOf[JUnitRunner])
class ApiUnitTests extends FlatSpec with Matchers with ScalatestRouteTest with ExchangeRateService {
  override def config: Config = testConfig

  override val defaultCachingSettings: CachingSettings = CachingSettings(system)


  implicit val timeout: RouteTestTimeout = RouteTestTimeout(duration = 5.seconds.dilated)

  val euroBaseExchangeResponse = ExternalApiResponse("2018-10-29", Map("GBP" -> 0.88788), "EUR")
  val gbpBaseExchangeResponse = ExternalApiResponse("2018-10-29", Map("EUR" -> 1.1262783259), "GBP")

  val mockRequest1 = ExchangeRateRequest("EUR", "GBP", 100)
  val mockRequest2 = ExchangeRateRequest("GBP", "EUR", 100)
  val mockResponse2 = ExchangeRateResponse(1.1381, 1.1608619999999998, 1.1381)

  // Mock external call
  override lazy val externalApiFlow: Flow[HttpRequest, HttpResponse, NotUsed] = Flow[HttpRequest].map { request =>
    if (request.uri.toString().endsWith("EUR")) {
      HttpResponse(
        status = StatusCodes.OK,
        entity = marshal(euroBaseExchangeResponse)
      )
    }
    else if (request.uri.toString().endsWith("GBP")) {
      HttpResponse(
        status = StatusCodes.OK,
        entity = marshal(gbpBaseExchangeResponse)
      )
    }
    else {
      HttpResponse(
        status = StatusCodes.BadRequest,
        entity = marshal(ExternalApiError("Base currency not supported")))
    }
  }

  it should s"respond to convert request from EUR to GBP $mockRequest1" in {
    Post(s"/api/convert", mockRequest1) ~> routes ~> check {
      status shouldBe StatusCodes.OK
      contentType shouldBe ContentTypes.`application/json`

      val rate = euroBaseExchangeResponse.rates.getOrElse(mockRequest1.toCurrency, 1.00)
      responseAs[ExchangeRateResponse] shouldBe ExchangeRateResponse(rate, rate * mockRequest1.amount, mockRequest1.amount)
    }
  }

  it should s"respond to convert request from GBP to EUR $mockRequest2" in {
    Post(s"/api/convert", mockRequest2) ~> routes ~> check {
      status shouldBe StatusCodes.OK
      contentType shouldBe ContentTypes.`application/json`
      val rate = gbpBaseExchangeResponse.rates.getOrElse(mockRequest2.toCurrency, 1.00)
      responseAs[ExchangeRateResponse] shouldBe ExchangeRateResponse(rate, rate * mockRequest2.amount, mockRequest2.amount)
    }
  }

  it should s"respond with BadRequest when base currency is empty" in {
    Post(s"/api/convert", ExchangeRateRequest("", "EUR", 100)) ~> routes ~> check {
      status shouldBe StatusCodes.BadRequest
      contentType shouldBe ContentTypes.`application/json`
      responseAs[String] shouldBe "Base currency not supported"
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
