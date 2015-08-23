package com.theomn.cartography


import java.util.concurrent.Executors

import net.minecraft.entity.Entity
import net.minecraft.entity.player.{EntityPlayerMP, EntityPlayer}
import net.minecraft.server.MinecraftServer
import net.minecraftforge.client.MinecraftForgeClient
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.relauncher.{SideOnly, Side}
import org.apache.logging.log4j.LogManager

import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}

import net.minecraft.util.BlockPos
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.{PlayerTickEvent, WorldTickEvent, ServerTickEvent}

import com.theomn.cartography.Implicits._


case class Player(id: Int, name: String, pos: BlockPos)

object Player {
  def apply(entity: EntityPlayer) = new Player(
    entity.getEntityId,
    entity.getDisplayNameString,
    entity.getPosition)
}

@SideOnly(Side.SERVER)
class CartographyEventHandler {

    implicit val ec = new ExecutionContext {
      val logger = LogManager.getLogger("Cartography")
      val threadPool = Executors.newFixedThreadPool(1000)

      def execute(runnable: Runnable) = threadPool.submit(runnable)

      def reportFailure(t: Throwable): Unit = {
        logger.error(t.getMessage, t)
      }
    }
  
  def now(): Long = MinecraftServer.getCurrentTimeMillis
  
  val logger = LogManager.getLogger("Cartography")

  private var lastTick = now()
  val interval = 3000

  @SubscribeEvent
  def tick(e: WorldTickEvent) = {
    val world = e.world
    val currentTime = now()
    val delta = currentTime - lastTick
    if (delta > interval) {
      world.playerEntities.toArray
        .map(_.asInstanceOf[EntityPlayerMP])
        .map(Player(_))
        .filter(isMoving(_))
        .foreach { player: Player =>
        lastTick = currentTime
        logger.info(s"Player Tick: $delta")
        generateTile(player)
      }
    }
  }

//  @SubscribeEvent
  def tick(e: PlayerTickEvent) = {
    val player = Player(e.player)
    val currentTime = now()
    val delta = currentTime - lastTick
    if (delta > interval) {
      if(isMoving(player)) {
        lastTick = currentTime
        logger.info(s"Player Tick: $delta")
        generateTile(player)
      }
    }
  }


  val MOVE_THRESHOLD = 100
  private val lastPos = mutable.Map[Int, BlockPos]()


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
        this.synchronized {
          lastPos.update(player.id, newBlock)
        }
        true
      case Some(block) => if(distanceSq(block, newBlock) >= MOVE_THRESHOLD) {
        this.synchronized {
          lastPos.update(player.id, newBlock)
        }
        true
      } else false
    }
  }

  def generateTile(player: Player): Unit = {
    Future {
      val tile = MapTile(player.pos)
      logger.debug(tile.toString)
      tile.save()
    }
  }

}
