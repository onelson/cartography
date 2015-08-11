package com.theomn.cartography

import net.minecraft.util.BlockPos
import net.minecraftforge.fml.common.gameevent.PlayerEvent

import scala.collection.JavaConversions._

import org.apache.logging.log4j.LogManager

import controllers.Assets
import play.api.mvc.{Action, Results}
import play.api.routing.sird._
import play.core.server._

import net.minecraft.entity.player.EntityPlayer
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.{FMLInitializationEvent, FMLPreInitializationEvent, FMLServerStoppingEvent}
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent

import scala.collection.mutable


@Mod(modid="Cartography", name="Cartography", version="0.0.1-SNAPSHOT", modLanguage="scala", serverSideOnly=true)
object CartographyMod {

  val moveThreshold: Int = 10
  val logger = LogManager.getLogger("Cartography")

  var server: Option[NettyServer] = None
  val playerPositions = mutable.Map[EntityPlayer, BlockPos]()

  // TODO: lookup conf details for the netty server
  // TODO: prime the sqlite db
  @Mod.EventHandler
  def preInit(e: FMLPreInitializationEvent): Unit = {}

  @Mod.EventHandler
  def init(e: FMLInitializationEvent): Unit = {
    // TODO: use config (read during preInit) when creating server
    server = Some(NettyServer.fromRouter() {
      case GET(p"/") => Assets.versioned(path="/public", file="index.html")
      case GET(p"/index.html") => Assets.versioned(path="/public", file="index.html")
      case GET(p"/assets/$file*") => Assets.versioned(path="/public/assets", file=file)
    })
  }

  @Mod.EventHandler
  def shutdown(e: FMLServerStoppingEvent): Unit = {
    server.foreach(_.stop())
  }


  @Mod.EventHandler
  def onLogin(e: PlayerEvent.PlayerLoggedInEvent): Unit =
    playerPositions.update(e.player, e.player.getPosition)

  @Mod.EventHandler
  def onLogout(e: PlayerEvent.PlayerLoggedOutEvent): Unit =
    playerPositions.remove(e.player)

  @Mod.EventHandler
  def tick(e: WorldTickEvent): Unit =
    for (p <- e.world.playerEntities.map(_.asInstanceOf[EntityPlayer])
         if isMoving(p)) {
      updateTile(p.getPosition)
    }


  def isMoving(player: EntityPlayer): Boolean = {
    val lastPos = playerPositions(player)
    val diff = scala.math.abs(lastPos.toLong - player.getPosition.toLong)
    logger.debug(s"diff: $diff")
    false
  }

  def updateTile(pos: BlockPos) = {}

}
