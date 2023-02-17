package com.fp.org.stx

import java.util.concurrent.locks.{ReadWriteLock, ReentrantReadWriteLock}

object statistics {

  case class stx(min: Int, max: Int, sm: Int, avr: Int)

  val event = collection.mutable.HashMap.empty[String, stx]
  val lockW: ReadWriteLock = new ReentrantReadWriteLock()

  var min: Int = 0
  var max: Int = 0
  var sum: Int = 0
  var avr: Int = 0

  def Clear = {
    min = 0;max = 0;sum = 0;avr = 0
  }

  def calc(s: Int) = {
    min = if (min < s.asInstanceOf[Int]) min else s.asInstanceOf[Int]
    max = if (max > s.asInstanceOf[Int]) max else s.asInstanceOf[Int]
    sum = sum + s.asInstanceOf[Int]
    avr = if (max!=0) sum / max else 0
  }

  def business(k: String, s: Int) = {
    lockW.writeLock().lock()

    val iskey = (event contains k)
    println(
      s"Task: key: ${k} contains: ${iskey} value: ${s}"
    )
    if (!iskey) {
      min = 0;max = 0;sum = 0;avr = 0;calc(s)
      val l1 = stx(min, max, sum, avr)
      event += k -> l1
    } else {
      val l2 = event(k)

      max = l2.max;min = l2.min;sum = l2.sm;avr = l2.avr;calc(s)
      val l1 = stx(min, max, sum, avr)
      event += (k -> l1)
    }

    lockW.writeLock().unlock()
  }

  def getMax = max
  def getMin = min
  def getAvr = avr

  def getstx = {
    for ((k, v) <- event) {
      println(s"sensor-id: $k")
      println(
        s"min: ${v.min} max: ${v.max} min: ${v.min} sum: ${v.sm}"
      )
    }
  }
}
