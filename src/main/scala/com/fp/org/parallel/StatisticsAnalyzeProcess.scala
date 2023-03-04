package com.fp.org.parallel

object StatisticsAnalyzeProcess {

  import scala.collection.par._
  import com.fp.org.stx.processing

  import com.fp.org.df.reader._

  def startStx(a: String) = {
    println(s" 1) ============================================")
    val h: Vector[(String, String)] = Reading(a)
    val p = new processing

    val hv2 = h.map(a => {
      a.ParReduceF(a._1,
        {
          val task = if (a._2 == "NaN") {println(s" item: : ${a._1.toString}-${a._2.toString}");"0"} else a._2  // 0 : NaN, 0: not any NaN
          p.business(a._1, task.toString.toInt)
          a._2
        })

    })
      .setInsideCalc(false)
      .waitF

    println(s" Sensors with highest avg humidity: ")
    p.getstx
    p.clear
  }

}
