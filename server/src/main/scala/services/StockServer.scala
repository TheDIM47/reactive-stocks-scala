package services

import java.math.{RoundingMode, MathContext}

import akka.actor._
import common._

import scala.collection.immutable.TreeMap
import scala.util.Random

class StockServer extends Actor with ActorLogging {

  import StockServer._
  import context.dispatcher

  import scala.concurrent.duration._
  import scala.language.postfixOps

  val InitialDelay = 100.milliseconds
  val VerySlow = 5000.milliseconds
  val Slow = 2000.milliseconds
  val Fast = 100.milliseconds

  // Schedule random stock events
  val scheduler = context.system.scheduler.schedule(InitialDelay, Slow, self, RandomStockEvent)

  @throws[Exception](classOf[Exception]) override def postStop(): Unit = scheduler.cancel()

  override def receive: Receive = {
    case SubscribeEvent => {
      log.debug(s"GOT SubscribeEvent from $sender")
      subscribe(sender)
    }
    case StockInfoEvent(symbol) => {
      log.debug(s"GOT ${StockInfoEvent(symbol)} from $sender")
      stocks.get(symbol).map(stock => sender ! randomizeStock(symbol, stock))
      log.debug(s"Stocks Sent: ${stocks.get(symbol)}")
    }
    case RandomStockEvent => {
      log.debug(s"GOT RandomStockEvent from $sender")
      val stock = randomizeStock
      stockUsers.foreach(_ ! stock)
      log.debug(s"Random Stock Sent: $stock")
    }
    case Terminated(r) => unsubscribe(r)
    case UnsubscribeEvent => unsubscribe(sender)
    case x => log.error(s"Invalid event: $x")
  }

  def subscribe(r: ActorRef): Unit = {
    stockUsers += r
    context.watch(r)
    log.debug(s"Actor subscribed: $r")
    randomizeStocks.foreach(stock => r ! stock)
  }

  def unsubscribe(r: ActorRef): Unit = {
    context.unwatch(r)
    stockUsers -= r
    log.debug(s"Actor unsubscribed: $r")
  }

  // who will receive messages
  private var stockUsers = Set[ActorRef]()
}

object StockServer {
  def props: Props = Props(classOf[StockServer])

  // Re-Ask for concrete stock or create new stock record
  case class StockInfoEvent(symbol: String)

  // Process Stock changed event (random)
  case object RandomStockEvent

  // Subscribe UserConnection for stock changes
  case class SubscribeEvent(subscriber: String)

  // Unsubscribe UserConnection
  case class UnsubscribeEvent(unsubscriber: String)

  private[this] case class Info(current: Double, low: Double, high: Double)

  // Initial stocks values ("on-open")
  private var stocks = TreeMap[String, Info](
    "GM"   -> Info(  38.87,  38.87,  38.87),
    "GE"   -> Info(  25.40,  25.40,  25.40),
    "MSFT" -> Info( 125.40, 125.40, 125.40),
    "AAPL" -> Info( 225.10, 225.10, 225.10),
    "GGL"  -> Info( 143.22, 143.22, 143.22),
    "MCD"  -> Info(  97.05,  97.05,  97.05),
    "VKS"  -> Info(   7.37,   7.37,   7.37),
    "KO"   -> Info(  17.00,  17.00,  17.00),
    "UAL"  -> Info(  69.45,  69.45,  69.45),
    "WMT"  -> Info(  83.24,  83.24,  83.24),
    "AAL"  -> Info(  55.76,  55.76,  55.76),
    "LLY"  -> Info(  76.12,  76.12,  76.12),
    "JPM"  -> Info(  61.75,  61.75,  61.75),
    "BAC"  -> Info(  15.84,  15.84,  15.84),
    "BA"   -> Info( 154.50, 154.50, 154.50)
  )

  implicit var allowZeroChange: Boolean = true

  /** Generate random stock value change based on initial ("on open") stock value */
  private def randomChange(value: Double)(implicit allowZeroChange: Boolean): Double = {
    val maxChange = value * 0.01
    val delta = maxChange * (1.0 - random.nextDouble * 2.0)
    val change = BigDecimal(value + delta).rounded(FOUR_DIGITS)
    change.toDouble
  }

  /** Randomize "on-open" stocks */
  def randomizeStocks = for (e <- stocks) yield randomizeStock(e._1, e._2)

  /** Get random stock */
  def randomizeStock: StockInfo = {
    val e = stocks.drop(random.nextInt(stocks.size)).head
    randomizeStock(e._1, e._2)
  }

  /** Change stock value with random */
  def randomizeStock(symbol: String, info: Info): StockInfo = {
    val v = randomChange(info.current)
    val f = Info(current = v, low = scala.math.min(info.low, v), high = scala.math.max(info.high, v))
    stocks += (symbol -> f)
    StockInfo(symbol, current = f.current, low = f.low, high = f.high, diff = info.current - v)
  }

  private[this] val random = new Random
  private[this] val FOUR_DIGITS = new MathContext(4, RoundingMode.HALF_UP)
}
