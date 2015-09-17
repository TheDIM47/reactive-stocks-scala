package controllers

import play.api.libs.json.JsValue
import play.api.mvc.{Action, Controller, WebSocket}
import play.libs.Akka
import services.{UserConnection, StockServer}

object Application extends Controller {
  import play.api.Logger
  import play.api.Play.current

  // create stock server
  val stockServer = Akka.system.actorOf(StockServer.props)

  def index = Action {
    Ok(views.html.index("REACTive Stocks Demo"))
  }

  def socket = WebSocket.acceptWithActor[String, JsValue] { request => out =>
    UserConnection.props(stockServer, out)
  }

  def logging = Action(parse.anyContent) { implicit request =>
    request.body.asJson.foreach { msg =>
      Logger.info(s"CLIENT - $msg")
    }
    Ok("")
  }
}
