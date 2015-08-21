package com.theomn.cartography

import java.awt.image.BufferedImage
import scala.collection.JavaConversions._

import net.minecraft.util.BlockPos
import net.minecraft.world.WorldServer


class MapTile(val x: Int, val y: Int, val world: WorldServer) {
  import MapTile.TILE_SIZE

  override def toString = s"MapTile(x: $x, y: $y)"

  // XXX: Just for testing... remove when writing to db
  def imgFileName = s"tile-${world.getWorldInfo.getWorldName}-$x-$y.png"

  def getImage(zoomLevel: Int): BufferedImage = {
    val img = new BufferedImage(TILE_SIZE, TILE_SIZE, BufferedImage.TYPE_INT_RGB)
    img.setRGB(0, 0, TILE_SIZE, TILE_SIZE, getBlocks(zoomLevel=0).map(getRGB).toArray, 0, TILE_SIZE)
    img
  }

  private def getRGB(pos: BlockPos): Int = {
    val top = world.getTopSolidOrLiquidBlock(pos)
    val chunk =
      world.getChunkFromBlockCoords(top)
    chunk.getBlock(top).getMaterial.getMaterialMapColor.colorValue
  }

  private def getBlocks(zoomLevel: Int): Seq[BlockPos] = {
    val xRange = (TILE_SIZE * x) until (TILE_SIZE * (1 + x))
    val yRange = (TILE_SIZE * y) until (TILE_SIZE * (1 + y))
    (for {blockY <- yRange.sortBy(-_)}
     yield xRange.map {new BlockPos(_, 0, blockY)}).flatten
  }
}

object MapTile {
  val TILE_SIZE = 256
  def apply(x: Int, y: Int, world: WorldServer) = new MapTile(x, y, world)
  def apply(pos: BlockPos, world: WorldServer): MapTile = MapTile(pos.getX / TILE_SIZE, pos.getZ / TILE_SIZE, world)
}
