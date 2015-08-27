package com.theomn.cartography


import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.server.MinecraftServer
import net.minecraft.world.World
import net.minecraftforge.fml.relauncher.{SideOnly, Side}
import org.apache.logging.log4j.LogManager

import net.minecraft.util.BlockPos
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent

import scala.collection.mutable


case class Player(id: Int, name: String, pos: BlockPos, world: World)

object Player {
  def apply(entity: EntityPlayerMP) = new Player(
    entity.getEntityId,
    entity.getDisplayNameString,
    entity.getPosition,
    entity.worldObj)
}

@SideOnly(Side.SERVER)
class CartographyEventHandler {

  val MOVE_THRESHOLD = 100
  private val lastPos = mutable.Map[Int, BlockPos]()
  
  def now(): Long = MinecraftServer.getCurrentTimeMillis
  
  val logger = LogManager.getLogger("Cartography")

  private var lastTick = now()
  val interval = 10000
  var processing: Boolean = false

  def activePlayers: Array[Player] =
    MinecraftServer
      .getServer
      .getConfigurationManager
      .playerEntityList
      .toArray
      .map(_.asInstanceOf[EntityPlayerMP])
      .map(Player(_))

  @SubscribeEvent
  def tick(e: ServerTickEvent): Unit = {
    val currentTime = now()
    val delta = currentTime - lastTick
    if (!processing && delta > interval) {
      processing = true
      try {
        logger.info(s"Server Tick: $delta")
        activePlayers.filter(isMoving).foreach(generateTile)
        lastTick = currentTime
      } finally processing = false
    }
  }

  def distanceSq(a: BlockPos, b: BlockPos): Int = {
    val x = a.getX - b.getX
    val z = a.getZ - b.getZ
    (x * x) + (z * z)
  }

  /** Gross side-effect, but this method is used to filter the player out that
    * will not produce tiles this tick as well as ensuring our store of last
    * positions are up to date.
    */
  def isMoving(player: Player): Boolean = {

    val newBlock = player.pos
    val oldBlock = lastPos.get(player.id)

    oldBlock match {
      case None =>
        lastPos.synchronized {
          lastPos.update(player.id, newBlock)
        }
        true
      case Some(block) => if(distanceSq(block, newBlock) >= MOVE_THRESHOLD) {
        lastPos.synchronized {
          lastPos.update(player.id, newBlock)
        }
        true
      } else false
    }
  }

  def generateTile(player: Player): Unit = {
    var tile = MapTile(player)
    logger.debug(tile.toString)
    tile.save()
    tile = null
  }
}
