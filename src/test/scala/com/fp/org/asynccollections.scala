package com.fp.org

import org.scalatest.FlatSpec
import com.fp.org.stx.processing
import com.fp.org.df.reader._
import scala.collection.par._

class asynccollections extends FlatSpec {
  behavior of "par collection size check"

  it should "checking par vector size " in {
    var size = 0
    Reading("leader-1.csv").map(a => {
      a.ParReduceF(a._1, {size=1; a._2})

      .setInsideCalc(false)
      .waitF
    assert(size>0 , "found size > 0")
    })

  }

}
