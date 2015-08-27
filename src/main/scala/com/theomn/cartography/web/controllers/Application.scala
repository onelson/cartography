package com.theomn.cartography.web.controllers


import akka.actor.ActorSystem
import akka.event.Logging
import akka.stream.ActorMaterializer
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._

import slick.driver.H2Driver.api._
import spray.json.DefaultJsonProtocol

import com.theomn.cartography.DB
import com.theomn.cartography.models.{DBTile, tiles}

object JsonProtocol extends DefaultJsonProtocol {
  implicit val tileFormat = jsonFormat4(DBTile.apply)
}

import com.theomn.cartography.Executors.threadpoolContext


object Application extends App {

  import JsonProtocol._
  implicit val system = ActorSystem("cartogarphy")
  implicit val materializer = ActorMaterializer()
  val logger = Logging(system, getClass)

  val route = path("tiles") {
      get {
        val db = DB.getConn
        complete {
          ToResponseMarshallable {
            db.run(tiles.all.result)
          }
        }
      }
    }

  val bindingFuture = Http().bindAndHandle(route, "localhost", 9000)

  def stop() = {
    system.shutdown()
    DB.shutdown()
  }

  def start() = {
    main(args=Array[String]())
    logger.info("started akka-http")
  }


}