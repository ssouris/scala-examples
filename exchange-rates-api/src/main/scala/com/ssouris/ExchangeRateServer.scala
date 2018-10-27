package com.ssouris

import akka.actor.ActorSystem
import akka.http.caching.scaladsl.CachingSettings
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.typesafe.config.{Config, ConfigFactory}

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.io.StdIn


object ExchangeRateServer extends App with ExchangeRateService {

  override implicit val system: ActorSystem = ActorSystem("exchange-rate-system")
  override implicit val executor: ExecutionContextExecutor = system.dispatcher
  override implicit val materializer: ActorMaterializer = ActorMaterializer()
  override val config: Config = ConfigFactory.load()
  override val defaultCachingSettings: CachingSettings = CachingSettings(system)

  val bindingFuture: Future[Http.ServerBinding] = Http().bindAndHandle(routes, "localhost", 8080)

  println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
  StdIn.readLine()

  bindingFuture
    .flatMap(_.unbind())
    .onComplete(_ => system.terminate())

}
