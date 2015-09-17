package models

import common.StockInfo
import play.api.mvc.WebSocket.FrameFormatter
import services.StockServer.{SubscribeEvent, UnsubscribeEvent}

object Formats {

  import play.api.libs.functional.syntax._
  import play.api.libs.json._

  /** Stock */

//  implicit val stockWrites = new Writes[Stock] {
//    def writes(v: Stock) = Json.obj( "symbol" -> v.symbol, "value" -> v.value )
//  }
//
//  implicit val stockReads: Reads[Stock] = (
//    (JsPath \ "symbol").read[String] and
//    (JsPath \ "value").read[Double]
//  )(Stock.apply _)
//
//  implicit val stockFormat = Json.format[Stock]
//  implicit val stockFrameFormatter = FrameFormatter.jsonFrame[Stock]
//
//  /** StockInfo */
//
//  implicit val stockInfoWrites = new Writes[StockInfo] {
//    def writes(v: StockInfo) = Json.obj(
//      "current" -> v.current, "diff" -> v.diff, "high" -> v.high, "low" -> v.low, "open" -> v.open
//    )
//  }

  implicit val stockInfoReads: Reads[StockInfo] = (
//    (JsPath \ "open").read[Double] and
    (JsPath \ "symbol").read[String] and
    (JsPath \ "current").read[Double] and
    (JsPath \ "low").read[Double] and
    (JsPath \ "high").read[Double] and
    (JsPath \ "diff").read[Double]
  )(StockInfo.apply _)

  implicit val stockInfoFormat = Json.format[StockInfo]
  implicit val stockInfoFrameFormatter = FrameFormatter.jsonFrame[StockInfo]

  /** SubscribeEvent */

  implicit val subscribeWrites = new Writes[SubscribeEvent] {
    def writes(v: SubscribeEvent) = Json.obj("subscriber" -> v.subscriber)
  }

//  implicit val subscribeReads: Reads[SubscribeEvent] = ( (JsPath \ "subscriber").read[String] )(SubscribeEvent.apply _)

  implicit val subscribeFormat = Json.format[SubscribeEvent]
  implicit val subscribeFrameFormatter = FrameFormatter.jsonFrame[SubscribeEvent]

  /** UnsubscribeEvent */

  implicit val unsubscribeWrites = new Writes[UnsubscribeEvent] {
    def writes(v: UnsubscribeEvent) = Json.obj("unsubscriber" -> v.unsubscriber)
  }

//  implicit val unsubscribeReads: Reads[UnsubscribeEvent] = ( (JsPath \ "unsubscriber").read[String] )(UnsubscribeEvent.apply _)

  implicit val unsubscribeFormat = Json.format[UnsubscribeEvent]
  implicit val unsubscribeFrameFormatter = FrameFormatter.jsonFrame[UnsubscribeEvent]

}
