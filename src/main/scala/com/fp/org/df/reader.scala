package com.fp.org.df

import scala.io.Source

object reader {

  trait SensorReader {
    def readSales(): Seq[Sensor]
  }
  case class Sensor(sensorid: String, humidity: String)
  class SensorCSVReader(val fileName: String) extends SensorReader {

    override def readSales(): Seq[Sensor] = {
      for {
        line <- Source.fromFile(fileName).getLines().drop(1).toVector
        values = line.split(",").map(_.trim)
      } yield Sensor(values(0), values(1))
    }

  }

  def Reading(f: String): Vector[(String, String)] = {
    var h: Vector[(String, String)] = Vector.empty
    val sonsor = new SensorCSVReader(f).readSales
    sonsor.foreach(a=> {
      println(s" sensor-id ${a.sensorid.toString}; humidity : ${a.humidity.toString} ")
      h = h :+ (a.sensorid -> a.humidity)
    })
    h
  }


}
