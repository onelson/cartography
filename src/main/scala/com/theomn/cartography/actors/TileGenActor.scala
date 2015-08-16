package com.theomn.cartography.actors


import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.server.MinecraftServer
import net.minecraft.util.BlockPos
import net.minecraft.world.WorldServer

import scala.collection.mutable
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

  override def receive = {
    case "tick" =>
      system.scheduler.scheduleOnce(5000 millis, self, "tick")
      val game = MinecraftServer.getServer

      for {
        worldServer <- game.worldServers
        player <- worldServer.playerEntities.map(_.asInstanceOf[EntityPlayerMP])
        if isMoving(player)
      } generateTile(player.getPosition, worldServer)

    case x => logger.warning("Got unknown message: {}", x)
  }

  val THRESHOLD = 100
  private val lastPos = mutable.Map[EntityPlayerMP, BlockPos]()

  def distanceSq(a: BlockPos, b: BlockPos): Int = {
    val x = a.getX - b.getX
    val z = a.getZ - b.getZ
    (x * x) + (z * z)
  }

  /** Gross side-effect, but this method is used to filter the player out that
    * will not produce tiles this tick as well as ensuring our store of last
    * positions are up to date.
    */
  def isMoving(player: EntityPlayerMP): Boolean = {
    val newBlock = player.getPosition
    val oldBlock = lastPos.get(player)

    oldBlock match {
      case None =>
        lastPos.update(player, newBlock)
        true
      case Some(block) => if(distanceSq(block, newBlock) >= THRESHOLD) {
        lastPos.update(player, newBlock)
        true
      } else false
    }

  }

  def generateTile(pos: BlockPos, world: WorldServer): Unit = {
    logger.warning("want to gen {}", pos)

    val top = world.getTopSolidOrLiquidBlock(pos)
    val chunk =
      world.getChunkFromBlockCoords(top)

    val block = chunk.getBlock(top)
    val rgb = block.getMaterial.getMaterialMapColor.colorValue
    logger.debug("{} @ {}", rgb, top)
  }

}
