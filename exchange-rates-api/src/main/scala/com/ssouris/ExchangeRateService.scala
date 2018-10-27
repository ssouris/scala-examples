package com.ssouris

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives.{as, complete, entity, pathPrefix, post, _}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer
import akka.stream.scaladsl.{Flow, Sink, Source}
import com.typesafe.config.Config

import scala.concurrent.{ExecutionContextExecutor, Future}

trait ExchangeRateService extends Protocols with ExceptionHandling with ExternalApiResponseCache {

  implicit val system: ActorSystem
  implicit def executor: ExecutionContextExecutor
  implicit val materializer: Materializer


  def config: Config

  // App Routes

  val routes: Route = (post & pathPrefix("api" / "convert")) {
    entity(as[ExchangeRateRequest]) { req =>
      complete(
        externalApiCache.getOrLoad(req.fromCurrency, getLatestExchangeRates)
          .map(r => r.rates.get(req.toCurrency) match {
            case Some(rate) => ExchangeRateResponse(rate, rate * req.amount, req.amount)
            case None => throw new ToCurrencyNotSupported(s"To Currency '${req.toCurrency}' not supported")
          }
          )
      )
    }
  }

  // Exchange Rate Api request

  lazy val externalApiFlow: Flow[HttpRequest, HttpResponse, Any] =
    Http().outgoingConnectionHttps(
      config.getString("services.exchange-rate-api.host")
    )

  def exchangeRateApiRequest(request: HttpRequest): Future[HttpResponse] =
    Source.single(request).via(externalApiFlow).runWith(Sink.head)

  private def getLatestExchangeRates(fromCurrency: String): Future[ExternalApiResponse] = {
    exchangeRateApiRequest(RequestBuilding.Get(s"/latest?base=$fromCurrency"))
      .flatMap { resp =>
        resp.status match {
          case StatusCodes.OK => Unmarshal(resp.entity).to[ExternalApiResponse]
          case StatusCodes.BadRequest => Unmarshal(resp.entity).to[ExternalApiError].flatMap {
            entity => throw new ExchangeRateApiBadRequest(StatusCodes.BadRequest.intValue, entity.error)
          }
          case statusCode => Future.failed(new ExchangeRateApiGenericException(statusCode.intValue()))
        }
      }
  }

}
