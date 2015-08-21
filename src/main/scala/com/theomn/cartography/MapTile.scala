package com.theomn.cartography

import java.awt.image.BufferedImage

import net.minecraft.util.BlockPos
import net.minecraft.world.WorldServer


class MapTile(x: Int, y: Int)(implicit world: WorldServer) {
  override def toString = s"MapTile(x: $x, y: $y)"

  def getImage(zoomLevel: Int): BufferedImage = ???
  private def getBlocks(zoomLevel: Int): Seq[BlockPos] = ???
}

object MapTile {
  val TILE_SIZE = 256

  def apply(x: Int, y: Int) = new MapTile(x, y)
  def apply(pos: BlockPos): MapTile = MapTile(pos.getX / TILE_SIZE, pos.getZ / TILE_SIZE)

}

