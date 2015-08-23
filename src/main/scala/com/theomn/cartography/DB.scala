package com.theomn.cartography

import slick.driver.H2Driver.api._
import com.theomn.cartography.Implicits._
import com.theomn.cartography.models.tiles


object DB {
  lazy private[this] val db = Database.forConfig("h2mem")
  def getConn = db

  lazy val schema = tiles.schema
  def setup() = {
    val db = getConn
    db.run(DBIO.seq(schema.create))
  }
}
