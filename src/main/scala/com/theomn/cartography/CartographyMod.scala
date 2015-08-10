package com.theomn.cartography

import com.theomn.cartography.web.controllers.Application
import controllers.Assets
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.{FMLInitializationEvent, FMLPreInitializationEvent, FMLServerStoppingEvent}
import org.apache.logging.log4j.LogManager
import play.api.mvc.{Action, Results}
import play.api.routing.sird._
import play.core.server._


@Mod(modid="Cartography", name="Cartography", version="0.0.0-SNAPSHOT", modLanguage="scala")
object CartographyMod {

  val logger = LogManager.getLogger("Cartography")

  var server: Option[NettyServer] = None

  @Mod.EventHandler
  def preInit(e: FMLPreInitializationEvent) {
    // TODO: lookup conf details for the netty server
 }

  @Mod.EventHandler
  def init(e: FMLInitializationEvent) {
    // TODO: use config (read during preInit) when creating server
    server = Some(NettyServer.fromRouter() {
      case GET(p"/") => Assets.versioned(path="/public", file="index.html")
      case GET(p"/index.html") => Assets.versioned(path="/public", file="index.html")
      case GET(p"/assets/$file*") => Assets.versioned(path="/public/assets", file=file)
    })
  }

  @Mod.EventHandler
  def shutdown(e: FMLServerStoppingEvent) {
    server.foreach(_.stop())
  }

}
