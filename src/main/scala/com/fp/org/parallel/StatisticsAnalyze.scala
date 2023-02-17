package com.fp.org.parallel

object StatisticsAnalyze extends App {

  import scala.collection.par._

  import com.fp.org.df.reader._
  import com.fp.org.stx.statistics._

  println(s" 1) ============================================")
  val h: Vector[(String, String)] = Reading("leader-1.csv")

  val hv2 = h.map(a => {
    a.ParReduceF(a._1,
    {
      val task = if (a._2 == "NaN") {println(s" item: : ${a._1.toString}-${a._2.toString}");"0"} else a._2  // 0 : NaN, 0: not any NaN
      business(a._1, task.toString.toInt)
      a._2
    })

  })
    .setInsideCalc(false)
    .waitF

  println(s" Sensors with highest avg humidity: ")
  getstx
}

/**
1) ============================================
 sensor-id s1; humidity : 10 
 sensor-id s2; humidity : 88 
 sensor-id s1; humidity : NaN 
 sensor-id s1; humidity : 20 
 item: : s1-NaN
Task: key: s1 contains: false value: 10
Task: key: s1 contains: true value: 0
Task: key: s1 contains: true value: 20
Task: key: s2 contains: false value: 88
ended ...
 Sensors with highest avg humidity: 
sensor-id: s1
min: 0 max: 20 min: 0 sum: 30
sensor-id: s2
min: 0 max: 88 min: 0 sum: 88
*/