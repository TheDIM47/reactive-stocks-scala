package services

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import common._
import play.api.libs.json.Json
import services.StockServer._

object UserConnection {
  def props(stockServer: ActorRef, out: ActorRef): Props = Props(classOf[UserConnection], stockServer, out)
}

class UserConnection(stockServer: ActorRef, out: ActorRef) extends Actor with ActorLogging {
  override def preStart() {
    log.debug(s"Sending Subscribe event")
    self ! SubscribeEvent
  }

  @throws[Exception](classOf[Exception])
  override def postStop(): Unit = {
    stockServer ! UnsubscribeEvent
  }

  import models.Formats._

  // RandomStockEvent
  override def receive: Receive = {
    // Subscribe on Stock Server events
    case SubscribeEvent => {
      stockServer ! SubscribeEvent
      log.debug(s"Got Subscribe event")
    }
    // send StockInfo(...) - extended version
    case info: StockInfo => {
      log.debug(s"GOT $info")
      out ! Json.toJson(info)
    }
    // ask for stock
    case StockInfoEvent(symbol) => {
      log.debug(s"GOT ${StockInfoEvent(symbol)}")
      stockServer ! StockInfoEvent(symbol)
    }
    // Unsubscribe from Stock Server
    case UnsubscribeEvent => {
      log.debug(s"GOT UnsubscribeEvent")
      stockServer ! UnsubscribeEvent
    }
    case x => log.error(s"IGNOREME: got: $x")
  }
}
