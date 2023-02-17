package com.fp.org.parallel

object StatisticsAnalyzeChaine extends App {

  import scala.collection.par._

  import com.fp.org.df.reader._
  import com.fp.org.stx.statistics._

  println(s" 2) ============================================")
  println(s" First file : leader-2.csv")
  val h2: Vector[(String, String)] = Reading("leader-2.csv")

  val hv2 = h2.map(a => {
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
2) ============================================
 First file : leader-2.csv
 sensor-id s2; humidity : 80 
 sensor-id s3; humidity : NaN 
 sensor-id s2; humidity : 78 
 sensor-id s1; humidity : 98 
 item: : s3-NaN
Task: key: s2 contains: false value: 80
Task: key: s2 contains: true value: 78
Task: key: s1 contains: false value: 98
Task: key: s3 contains: false value: 0
ended ...
 Sensors with highest avg humidity: 
sensor-id: s3
min: 0 max: 0 min: 0 sum: 0
sensor-id: s1
min: 0 max: 98 min: 0 sum: 98
sensor-id: s2
min: 0 max: 80 min: 0 sum: 158

Process finished with exit code 0

*/
