package com.theomn.cartography.web.controllers


import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.model.{HttpResponse, ContentType, MediaType}
import akka.stream.ActorMaterializer
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes._
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
    } ~
    path("tiles" / Segment / IntNumber / IntNumber / IntNumber) {
      (worldName, col, row, zoomLevel) =>
        get {
          val q = tiles.
            filter(_.col === col)
            .filter(_.row === row)
            .filter(_.zoomLevel === zoomLevel)


          DB.getConn.run(q.result.headOption).map {

            case Some(data) =>
              val img = ???
              val resp = HttpResponse(OK, entity=img)
              complete(OK, resp.entity.withContentType(ContentType(MediaType.`image/png`)))
            case _ =>
              complete(NotFound, "tile not found")
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