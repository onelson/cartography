package com.theomn.cartography.models


import slick.driver.H2Driver.api._
import com.theomn.cartography.Executors.threadpoolContext


case class DBTile(zoomLevel: Int, col: Int, row: Int, data: String)

class TilesTable(tag: Tag) extends Table[DBTile](tag, "tiles") {
  def zoomLevel = column[Int]("zoomLevel")
  def col = column[Int]("tile_column")
  def row = column[Int]("tile_row")
  def data = column[String]("tile_data")
  def pk = primaryKey("pk", (zoomLevel, col, row))
  def * = (zoomLevel, col, row, data) <> (DBTile.tupled, DBTile.unapply)
}

object tiles extends TableQuery(new TilesTable(_)) {
  def all = for { tile <- tiles } yield tile
}
