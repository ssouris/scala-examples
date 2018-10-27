package com.ssouris

import spray.json.{DefaultJsonProtocol, PrettyPrinter, RootJsonFormat}

trait Protocols extends DefaultJsonProtocol {
  // Pretty print json
  implicit val printer: PrettyPrinter.type = PrettyPrinter

  // micro-service
  implicit val exchangeRateRequestFormat: RootJsonFormat[ExchangeRateRequest] = jsonFormat3(ExchangeRateRequest)
  implicit val exchangeRateResponseFormat: RootJsonFormat[ExchangeRateResponse] = jsonFormat3(ExchangeRateResponse)

  // external api
  implicit val externalApiResponseFormat: RootJsonFormat[ExternalApiResponse] = jsonFormat3(ExternalApiResponse)
  implicit val externalApiErrorFormat: RootJsonFormat[ExternalApiError] = jsonFormat1(ExternalApiError)
}
