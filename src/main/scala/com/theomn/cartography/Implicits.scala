package com.theomn.cartography


import java.util.concurrent.Executors
import net.minecraft.server.MinecraftServer
import org.apache.logging.log4j.LogManager

import scala.concurrent._

import com.theomn.cartography.models.DBTile


object Implicits {
  implicit val mcContext = new ExecutionContext {

    val logger = LogManager.getLogger("Cartography")

    def execute(runnable: Runnable) =
      MinecraftServer.getServer.addScheduledTask(runnable)

    def reportFailure(t: Throwable): Unit = {
      logger.error(t.getMessage, t)
    }
  }

//  implicit val ec = new ExecutionContext {
//    val logger = LogManager.getLogger("Cartography")
//    val threadPool = Executors.newFixedThreadPool(50000)
//
//    def execute(runnable: Runnable) = threadPool.submit(runnable)
//
//    def reportFailure(t: Throwable): Unit = {
//      logger.error(t.getMessage, t)
//    }
//  }

//
//  implicit val tileWrites: Writes[DBTile] = (
//    (JsPath \ "zoomLevel").write[Int] and
//      (JsPath \ "column").write[Int] and
//      (JsPath \ "row").write[Int] and
//      (JsPath \ "data").write[String]
//  )(unlift(DBTile.unapply))

}
