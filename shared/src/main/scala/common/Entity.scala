package common

// Stock: Open, Interval, Current, Diff
case class StockInfo(symbol: String,
                     current: Double,
                     // open: Double,
                     low: Double,
                     high: Double,
                     diff: Double)

