package com.theomn.cartography

import org.apache.logging.log4j.LogManager

import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.{FMLInitializationEvent, FMLPreInitializationEvent, FMLServerStoppingEvent}

import akka.actor.Props
import play.api.mvc.{Action, Results}
import play.api.routing.sird._
import play.core.server._
import play.api.libs.concurrent.Akka
import controllers.Assets

import com.theomn.cartography.actors.TileGenActor
import com.theomn.cartography.web.controllers.Application

@Mod(
  modid="cartography",
  name="Cartography",
  version="0.0.1-SNAPSHOT",
  modLanguage="scala",
  serverSideOnly=true,
  acceptableRemoteVersions="*")
object CartographyMod {

  val logger = LogManager.getLogger("Cartography")
  var server: Option[NettyServer] = None

  // TODO: lookup conf details for the netty server
  // TODO: prime the sqlite db
  @Mod.EventHandler
  def preInit(e: FMLPreInitializationEvent): Unit = {}

  @Mod.EventHandler
  def init(e: FMLInitializationEvent): Unit = {
    Application
    // TODO: use config (read during preInit) when creating server
    server = Some(NettyServer.fromRouter() {
      case GET(p"/") => Assets.versioned(path="/public", file="index.html")
      case GET(p"/index.html") => Assets.versioned(path="/public", file="index.html")
      case GET(p"/assets/$file*") => Assets.versioned(path="/public/assets", file=file)
      case GET(p"/todo*") => Application.todo
    })

    import play.api.Play.current

    val tileGenActor = Akka.system.actorOf(
      Props[TileGenActor],
      name="tileGenActor")
  }

  @Mod.EventHandler
  def shutdown(e: FMLServerStoppingEvent): Unit = {
    server.foreach(_.stop())
  }
}
