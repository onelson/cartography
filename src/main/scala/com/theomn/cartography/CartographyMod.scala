package com.theomn.cartography

import org.apache.logging.log4j.LogManager

import net.minecraftforge.fml.common.{FMLCommonHandler, Mod}
import net.minecraftforge.fml.common.event.{FMLPostInitializationEvent, FMLInitializationEvent, FMLPreInitializationEvent, FMLServerStoppingEvent}

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

  // TODO: lookup conf details for the netty server
  // TODO: prime the sqlite db
  @Mod.EventHandler
  def preInit(e: FMLPreInitializationEvent): Unit = {}

  @Mod.EventHandler
  def init(e: FMLInitializationEvent): Unit = {
    DB.setup()
    Application.start()
  }

  @Mod.EventHandler
  def serverTick(e: FMLPostInitializationEvent) = {
    FMLCommonHandler.instance().bus().register(new CartographyEventHandler())

  }

  @Mod.EventHandler
  def shutdown(e: FMLServerStoppingEvent): Unit = {
  }
}
