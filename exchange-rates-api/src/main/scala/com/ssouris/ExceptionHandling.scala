package com.ssouris

import akka.http.javadsl.model.HttpEntities
import akka.http.scaladsl.model.{ContentTypes, HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives.{complete, extractRequest}
import akka.http.scaladsl.server.ExceptionHandler

trait ExceptionHandling {

  // Exception Handling

  implicit def exceptionHandler: ExceptionHandler =
    ExceptionHandler {
      case conversionNotSupported: ToCurrencyNotSupported =>
        extractRequest { _ =>
          complete(HttpResponse(
            status = StatusCodes.BadRequest,
            entity = HttpEntities.create(ContentTypes.`application/json`, conversionNotSupported.getMessage))
          )
        }
      case badRequest: ExchangeRateApiBadRequest =>
        extractRequest { _ =>
          complete(
            HttpResponse(
              status = StatusCodes.BadRequest,
              entity = HttpEntities.create(ContentTypes.`application/json`, badRequest.getMessage))
          )
        }
    }

  // Exceptions

  class ToCurrencyNotSupported(message: String) extends RuntimeException {
    override def getMessage: String = message
  }

  class ExchangeRateApiBadRequest(status: Integer, message: String) extends RuntimeException {
    override def getMessage: String = message
  }

  class ExchangeRateApiGenericException(status: Integer) extends RuntimeException {
    override def getMessage: String = s"Exchange API responded with $status"
  }

}
