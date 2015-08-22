package com.theomn.cartography.models

import java.sql.Blob

import slick.driver.H2Driver.api._

// zoom_level integer, tile_column integer, tile_row integer, tile_data blob

case class Tile(zoomLevel: Int, x: Int, y: Int, data: Blob, world: String)

class TilesTable(tag: Tag) extends Table[(Int, Int, Int, Blob, String)](tag, "tiles") {
  def zoomLevel = column[Int]("zoomLevel")
  def x = column[Int]("tile_column")
  def y = column[Int]("tile_row")
  def data = column[Blob]("tile_data")
  def world = column[String]("world")
  def * = (zoomLevel, x, y, data, world)
}

object tiles extends TableQuery(new TilesTable(_))
