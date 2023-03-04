package com.fp.org

import com.fp.org.stx.functions
import com.fp.org.stx.functions.IO
import org.scalatest.FlatSpec

class functionstest extends FlatSpec {
  behavior of "functions"

  it should "check max setting values with the related type of IO  " in {
    functions.product_max[IO](5).unsafeRun()
    assert(functions.max_base === 5, "found corrected")
  }

  it should "check min setting values with the related type of IO  " in {
    functions.product_min[IO](5).unsafeRun()
    assert(functions.min_base === 0, "found corrected")
  }

  it should "check sum setting values with the related type of IO  " in {
    functions.product_sum[IO](5).unsafeRun()
    assert(functions.sum_base === 5, "found corrected")
  }

  it should "check avr setting values with the related type of IO  " in {
    functions.product_avr[IO](5).unsafeRun()
    assert(functions.avr_base >= 0, "found corrected")
  }

}
