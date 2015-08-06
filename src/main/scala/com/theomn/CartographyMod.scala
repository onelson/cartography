package com.theomn


import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.{FMLPostInitializationEvent, FMLInitializationEvent, FMLPreInitializationEvent}
import org.apache.logging.log4j.LogManager


@Mod(modid="Cartography", name="Cartography", version="0.0.0-SNAPSHOT", modLanguage="scala")
object CartographyMod {

  val logger = LogManager.getLogger("Cartography")

  @Mod.EventHandler
  def preInit(e: FMLPreInitializationEvent) {
    logger.info("OMG I'm in the preInit method! Hooray!")
  }



  @Mod.EventHandler
  def init(e: FMLInitializationEvent) {
    logger.info("OMG I'm in the init method! Hooray!")
  }



  @Mod.EventHandler
  def postInit(e: FMLPostInitializationEvent) {
    logger.info("OMG I'm in the postInit method! Hooray!")
  }

}
