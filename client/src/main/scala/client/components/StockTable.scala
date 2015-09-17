package client.components

import common._
import japgolly.scalajs.react.vdom.prefix_<^._
import japgolly.scalajs.react.{BackendScope, ReactComponentB}
import org.scalajs.dom
import org.scalajs.dom._
import org.scalajs.dom.raw.WebSocket
import upickle.default._

case class State(m: Map[String, StockInfo])

// https://japgolly.github.io/scalajs-react/#examples/product-table
object StockTable {

  def getWebsocketUri(document: Document): String = {
    val wsProtocol = if (dom.document.location.protocol == "https:") "wss" else "ws"
    s"$wsProtocol://${dom.document.location.host}/ws"
  }

  val StockRow = ReactComponentB[StockInfo]("StockRow")
//    unwatch: function() { this.props.unwatchStockHandler(this.props.stock.symbol); },
    .render(s => {
        <.tr(
          <.td(s.symbol),
          <.td(/*^.className := changeClass,*/ s.current.formatted("%.4f")),
          <.td(<.span(s.diff.formatted("%.4f")), <.span(if (s.diff < 0) Icon.check else Icon.dashboard)),
          <.td(s.high.formatted("%.4f")),
          <.td(s.low.formatted("%.4f"))
        ) // tr
    }).build

  // render
  class Backend($: BackendScope[Map[String, StockInfo], State]) {
    def stop() = {}

    def start() = {
      val uri = getWebsocketUri(dom.document)
      val ws = new WebSocket(uri)
      ws.onopen = { (event: Event) => ws.send("hello") }
      ws.onclose = { (event: Event) => println(event) }
      ws.onerror = { (event: ErrorEvent) => println(event) }
      ws.onmessage = { (event: MessageEvent) =>
        val s: StockInfo = read[StockInfo](event.data.toString)
        println(s)
        values += s.symbol -> s
        $.modState(x => State(values))
      }
    }
  }

  val StocksArea = ReactComponentB[Map[String, StockInfo]]("StockArea")
    .initialState(State(Map.empty))
    .backend(new Backend(_))
    .render((P, S, B) => {
      <.div(^.className := "row",
        <.div(^.className := "col-sm-3 col-md-2 sidebar",
          <.ul(^.className := "nav nav-sidebar",
            <.li(^.className := "active", <.a(^.href := "#", "Overview", <.span(^.className := "sr-only"))),
            <.li(<.a(^.href := "#", "Reports")),
            <.li(<.a(^.href := "#", "Analytics")),
            <.li(<.a(^.href := "#", "Export"))
          ) // ul
        ), // div sidebar
        <.div(^.className := "table-responsive",
          <.table(^.id := "stocks-table", ^.className := "table-bordered",
            <.thead(
              <.tr(
                <.th("Name"),
                <.th("Quote"),
                <.th("Delta"),
                <.th("High"),
                <.th("Low")
              )),
            <.tbody(S.m.map(x => StockRow(x._2)))
          ) // table
        ) // div table
      ) // div row
    })
    .componentDidMount(_.backend.start())
    .componentWillUnmount(_.backend.stop())
    .build

  var values = Map[String, StockInfo]()

  def apply() = StocksArea(values)
}
