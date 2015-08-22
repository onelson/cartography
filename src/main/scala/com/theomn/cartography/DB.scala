package com.theomn.cartography


import slick.driver.H2Driver.api._
import scala.concurrent.ExecutionContext.Implicits.global


object DB {
  // TODO: ensure schema is setup before handing back a connection
  def get = Database.forConfig("h2mem")

  /**
   * Unsure this will actually be useful...
   * @param q query to run
   * @return Unit
   */
  def run(q: => Any) = try {
    implicit val db = get
    q
  } finally db.close
}
