package com.theomn.cartography.actors


import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.server.MinecraftServer

import scala.concurrent.duration._
import scala.collection.JavaConversions._
import akka.actor.Actor
import akka.event.Logging


class TileGenActor extends Actor {
  import context._

  val logger = Logging(system, this)


  override def preStart() = {
    logger.debug("Starting Actor with initial tick.")
    system.scheduler.scheduleOnce(500 millis, self, "tick")
  }

  // override postRestart so we don't call preStart and schedule a new message
  override def postRestart(reason: Throwable) = {}

  override def preRestart(reason: Throwable, message: Option[Any]) {
    logger.error(reason, "Restarting due to [{}] when processing [{}]",
      reason.getMessage, message.getOrElse(""))
  }

  def receive = {
    case "tick" => {
      system.scheduler.scheduleOnce(5000 millis, self, "tick")
      val game = MinecraftServer.getServer
      for {
        worldServer <- game.worldServers
        player <- worldServer.playerEntities.map(_.asInstanceOf[EntityPlayerMP])
      } logger.debug(s"{}: {}, @{}", worldServer.getWorldInfo.getWorldName, player.getDisplayNameString, player.getPosition)
    }
    case x => logger.warning("Got unknown message: {}", x)
  }
}
