package com.theomn.cartography


import java.io.File

import net.minecraft.server.MinecraftServer
import slick.driver.H2Driver.api._
import com.theomn.cartography.Executors.threadpoolContext
import com.theomn.cartography.models.tiles


object DB {
  lazy private[this] val db = {
    val dbfile = new File(MinecraftServer.getServer.getDataDirectory, "cartography.db").getAbsolutePath
    Database.forURL(
      s"jdbc:h2:file:$dbfile;DB_CLOSE_DELAY=-1",
      driver="org.h2.Driver",
      executor=AsyncExecutor("cartography-db", numThreads=10, queueSize=1000))
  }
  def getConn = db

  lazy val schema = tiles.schema
  def setup() = {
    val db = getConn
    db.run(DBIO.seq(schema.create))
  }

  def shutdown() = db.close()
}
