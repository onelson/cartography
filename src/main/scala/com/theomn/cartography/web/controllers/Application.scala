package com.theomn.cartography.web.controllers


import scala.concurrent.Future
import play.api.mvc._


object Application extends Controller {

  def index = Action.async {
    Future.successful(Ok("It works!"))
  }

}
