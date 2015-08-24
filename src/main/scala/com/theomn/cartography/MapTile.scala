package com.theomn.cartography

import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO
import javax.xml.bind.DatatypeConverter

import net.minecraft.util.BlockPos
import net.minecraft.world.World
import org.apache.logging.log4j.LogManager

import slick.driver.H2Driver.api._

import com.theomn.cartography.models.{tiles, DBTile}



class MapTile(val x: Int, val y: Int, world: World) {
  import MapTile.TILE_SIZE
  val logger = LogManager.getLogger("Cartography")

  override def toString = s"MapTile(x: $x, y: $y)"

  def getImage(zoomLevel: Int): BufferedImage = {
    val img = new BufferedImage(TILE_SIZE, TILE_SIZE, BufferedImage.TYPE_INT_RGB)
    img.setRGB(0, 0, TILE_SIZE, TILE_SIZE, getBlockColors(zoomLevel=0).toArray, 0, TILE_SIZE)
    img
  }

  private def getRGB(w: World, pos: BlockPos): Int = {
    val top = world.getTopSolidOrLiquidBlock(pos)
    val chunk =
      world.getChunkFromBlockCoords(top)
    val color = chunk.getBlock(top).getMaterial.getMaterialMapColor.colorValue
    color
  }

  private def getBlockColors(zoomLevel: Int): Seq[Int] = {
    val xRange = (TILE_SIZE * x) until (TILE_SIZE * (1 + x))
    val yRange = (TILE_SIZE * y) until (TILE_SIZE * (1 + y))

    (for {blockY <- yRange.sortBy(-_)} yield {
      xRange.map {
        new BlockPos(_, 0, blockY)
      }
    }).flatten.map(getRGB(world, _))

  }

  def save():Unit = {
    logger.info("writing {} to db.", this)
    val img = getImage(zoomLevel=0)
    val out = new ByteArrayOutputStream()
    ImageIO.write(img, "png", out)
    out.flush()
    val data = DatatypeConverter.printBase64Binary(out.toByteArray)
    val record = DBTile(0, x, y, data)
    val db = DB.getConn
    db.run(tiles += record)
  }

}

object MapTile {
  val TILE_SIZE = 256
  def apply(x: Int, y: Int, world: World) = new MapTile(x, y, world)
  def apply(pos: BlockPos, world: World): MapTile = MapTile(pos.getX / TILE_SIZE, pos.getZ / TILE_SIZE, world)
  def apply(player: Player): MapTile = MapTile(player.pos.getX / TILE_SIZE, player.pos.getZ / TILE_SIZE, player.world)
}
