package com.theomn.cartography.actors

import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.server.MinecraftServer
import net.minecraft.world.WorldServer
import org.apache.logging.log4j.LogManager

import scala.concurrent.duration._
import scala.collection.JavaConversions._
import akka.actor.Actor


class TileGenActor extends Actor {
  val logger = LogManager.getLogger("Cartography")

  import context._

  override def preStart() =
    system.scheduler.scheduleOnce(500 millis, self, "tick")

  // override postRestart so we don't call preStart and schedule a new message
  override def postRestart(reason: Throwable) = {}

  def receive = {
    case "tick" =>
      system.scheduler.scheduleOnce(1000 millis, self, "tick")

      val game = MinecraftServer.getServer
      for {
        worldServer <- game.worldServers
        player <- worldServer.playerEntities.map(_.asInstanceOf[EntityPlayerMP])
      } {
        // do stuff!
        player.getPosition
        logger.debug(s"${worldServer.getWorldInfo.getWorldName}: ${player.getDisplayNameString}, ${player.getPosition}")
      }

  }
}