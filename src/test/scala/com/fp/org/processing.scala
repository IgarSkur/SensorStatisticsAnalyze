package com.fp.org

import com.fp.org.stx.functions
import com.fp.org.stx.functions.IO
import org.scalatest.FlatSpec
import com.fp.org.stx.processing

class processingtest extends FlatSpec {
  behavior of "business"

  it should "check max setting values by business function  " in {
    val p = new processing
    p.business("s1", 5)
    assert(functions.event.size > 0 , "data founded")
    assert(functions.max_base === 5, "found corrected")
  }

  it should "check min setting values by business function  " in {
    val p = new processing
    p.business("s2", 5)
    assert(functions.event.size > 0 , "data founded")
    assert(functions.min_base === 0, "found corrected")
  }

  it should "check sum setting values by business function  " in {
    val p = new processing
    p.business("s3", 5)
    assert(functions.event.size > 0 , "data founded")
    assert(functions.sum_base === 5, "found corrected")
  }

  it should "check avr setting values by business function  " in {
    val p = new processing
    p.business("s4", 5)
    assert(functions.event.size > 0 , "data founded")
    assert(functions.avr_base >= 0, "found corrected")
  }


}
