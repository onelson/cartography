package com.theomn.cartography.web.controllers

import javax.xml.bind.DatatypeConverter

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.model.{MediaTypes, HttpResponse, ContentType}
import akka.stream.ActorMaterializer
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.marshallers.xml.ScalaXmlSupport._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._

import slick.driver.H2Driver.api._
import spray.json.DefaultJsonProtocol

import com.theomn.cartography.DB
import com.theomn.cartography.models.{DBTile, tiles}

object JsonProtocol extends DefaultJsonProtocol {
  implicit val tileFormat = jsonFormat4(DBTile.apply)
}

import com.theomn.cartography.Executors


object Application extends App {
  import JsonProtocol._
  implicit val ctx = Executors.threadpoolContext
  implicit val system = ActorSystem("cartogarphy-web")
  implicit val materializer = ActorMaterializer()
  val logger = Logging(system, getClass)
  val db = DB.getConn

  val route = pathSingleSlash {
    get{
      complete {
        <html>
          <body>Hello world!</body>
        </html>
      }
    }
  } ~
  (pathPrefix("tiles") & get) {
    pathEnd {
      complete {
        ToResponseMarshallable {
          db.run(tiles.all.result)
        }
      }
    } ~
    (path("grid") & get){
      // TODO: display grid of tile pics
      complete {
        ToResponseMarshallable {
          db.run(tiles.all.result)
        }
      }
    } ~
    (path(Segment / Segment / Segment / IntNumber) & get) {
      (worldName, col, row, zoomLevel) =>
        complete {
          val q = tiles
            .filter(_.col === col.toInt)
            .filter(_.row === row.toInt)
            .filter(_.zoomLevel === zoomLevel)
          for {result <- DB.getConn.run(q.result.headOption)} yield result match {
            case Some(tile: DBTile) =>
              val img = DatatypeConverter.parseBase64Binary(tile.data)
              val resp = HttpResponse(OK, entity = img)
              val respEntity =
                resp.entity.withContentType(ContentType(MediaTypes.`image/png`))
              HttpResponse(OK, entity = respEntity)
            case _ =>
              HttpResponse(NotFound, entity = "tile not found")
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