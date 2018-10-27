package com.ssouris

/**
  * API request object
  */
case class ExchangeRateRequest(fromCurrency: String, toCurrency: String, amount: Double)

/**
  * API response object
  */
case class ExchangeRateResponse(exchange: Double, amount: Double, original: Double)

/**
  * External API error response
  */
case class ExternalApiError(error: String)

/**
  * External API valid response object
  */
case class ExternalApiResponse(date: String, rates: Map[String, Double], base: String)

