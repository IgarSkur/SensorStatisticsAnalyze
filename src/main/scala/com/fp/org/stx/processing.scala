package com.fp.org.stx

class processing {

  import functions._

  def business(k: String, s: Int) = {
    lockW.writeLock().lock()

    val iskey = (event contains k)
    println(
      s"Task: key: ${k} contains: ${iskey} value: ${s}"
    )
    if (!iskey) {
      min_base = 0;max_base = 0;sum_base = 0;avr_base = 0;calc(s)
      val l1 = stx(min_base, max_base, sum_base, avr_base)
      event += k -> l1
    } else {
      val l2 = event(k)

      max_base = l2.max;min_base = l2.min;sum_base = l2.sm;avr_base = l2.avr;calc(s)
      val l1 = stx(min_base, max_base, sum_base, avr_base)
      event += (k -> l1)
    }

    lockW.writeLock().unlock()
  }

  def getMax = max_base
  def getMin = min_base
  def getAvr = avr_base
  def clear =  functions.clear

  def getstx = {
    for ((k, v) <- event) {
      println(s"sensor-id: $k")
      println(
        s"min: ${v.min} max: ${v.max} min: ${v.min} sum: ${v.sm}"
      )
    }

  }
}
