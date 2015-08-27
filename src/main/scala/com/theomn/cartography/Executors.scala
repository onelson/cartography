package com.theomn.cartography


import net.minecraft.server.MinecraftServer
import org.apache.logging.log4j.LogManager

import scala.concurrent._


object Executors {
  implicit val mcContext = new ExecutionContext {

    val logger = LogManager.getLogger("Cartography")

    def execute(runnable: Runnable) =
      MinecraftServer.getServer.addScheduledTask(runnable)

    def reportFailure(t: Throwable): Unit = {
      logger.error(t.getMessage, t)
    }
  }

  implicit val threadpoolContext = new ExecutionContext {
    val logger = LogManager.getLogger("Cartography")
    val threadPool = java.util.concurrent.Executors.newFixedThreadPool(50)

    def execute(runnable: Runnable) = threadPool.submit(runnable)

    def reportFailure(t: Throwable): Unit = {
      logger.error(t.getMessage, t)
    }
  }

}
