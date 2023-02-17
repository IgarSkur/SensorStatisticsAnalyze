package scala.collection.par

// Copyright (C) Igor Skuratov. All rights reserved.
// Licensed under the MIT License. See LICENSE in project root for information.

trait ParDefs {
  implicit def seq2ops[Repr](seq: Repr) = new ParDefs.ops(seq)
}

object ParDefs {

  import execIOFutures._

  class ops[Repr](val seq: Repr) extends AnyVal {

    def waitF  = {
      fWait

      seq
    }

    def ParF(f: => Any) = {
      ParFuture(f)

      seq
    }

    def ParReduceF(k: Any, f: => Any) = {
      ParPartitionFuture(k,f)

      seq
    }

    def getData = {
      lstout
    }

    def getMax = max
    def getMin = min
    def getAvr = avrv
    def setInsideCalc(turnUp : Boolean = true) = allListResults = turnUp
    def getTrace = Trace

  }

}

object execIOFutures {

  import scala.concurrent.Await
  import scala.concurrent.duration.Duration

  import java.util.concurrent.CopyOnWriteArrayList
  import cats.effect.{Async, Concurrent, ExitCode, IO, IOApp}
  import scala.util.{Failure, Success}

  import scala.concurrent.Future
  import scala.concurrent.ExecutionContext.Implicits.global
  import java.util.concurrent.locks.{ReadWriteLock, ReentrantReadWriteLock}

  @volatile var lst: List[Future[Future[Any]]] = List[Future[Future[Any]]]()
  val event = collection.mutable.HashMap.empty[Any,List[Future[Future[Any]]]]
  val lstout = new CopyOnWriteArrayList[Any]()
  val lockW: ReadWriteLock = new ReentrantReadWriteLock()

  var min: Any = 1
  var max: Any = 0
  var sum : Any = 0
  var avrv: Any = 0
  var allListResults = false

  def jobF(f: => Future[Any]) = {

    val promise = Future {
      val f_message = f
      f_message
    }

    promise
  }

  def remList[A](i:A, li:List[A]) = {
    val (head,_::tail) = li.span(i != _)
    head ::: tail
  }

  implicit def RichFormatter(string: String) = new {
    def richFormat(replacement: Map[String, Any]) =
      (string /: replacement) {(res, entry) => res.replaceAll("#\\{%s\\}".format(entry._1), entry._2.toString)}
  }

  def business(s: Int) = {
    min = if (min.asInstanceOf[Int] < s.asInstanceOf[Int]) min else s
    max = if (max.asInstanceOf[Int] > s.asInstanceOf[Int]) max else s
    sum = sum.asInstanceOf[Int] + s.asInstanceOf[Int]
    avrv = sum.asInstanceOf[Int] / max.asInstanceOf[Int]
  }

  def active(l: List[Future[Future[Any]]]): IO[Any] =
    IO.async { cb =>
      l.foreach(a => {
        a.foreach( u => {
          u.onComplete {
            case Success(s) => {
              cb(Right(s))
              if (allListResults) {
                business(s.toString.toInt)
                lstout.add(s)
              }
              lockW.writeLock().lock()
              lst = remList[Future[Future[Any]]](a, lst)
              lockW.writeLock().unlock()
              //println(s" trace ${s}")

            }
            case Failure(e) => cb(Left(e))
          }
        })

        Await.result({a}, Duration.Inf)

      })

    }

  def ParAsFuture(f: => Future[Any]) = {
    lst = lst ::: List(jobF(f))
  }

  def ParFuture(f: => Any) = {
    lst = lst ::: List(jobF(Future{f}))
  }

  def ParPartitionFuture(k:Any, f: => Any) = {
    val l1 = List(jobF(Future{f}))
    lst = lst ::: l1

    val iskey = (event contains k)
    if (!iskey)
      event += (k -> l1)
    else {
      val l2 = event(k)
      val lm = l2 ::: l1
      event += (k -> lm)
    }

  }

  def getData = {
    println(s" outstack : ${lstout}")
  }

  def getMax = max
  def getMin = min
  def getAvr = avrv
  def Trace = {
    for ((k,v) <- event) {
      println(s"key: $k")
      v.foreach(
        {
          a => println(
            s"values: ${a.value.get.get.value.get.get}"
            //s"values: ${a.value}"
          )
        }
      )
    }
  }

  def fWait = {
    active(lst).unsafeRunSync()
    while (!lst.isEmpty) {}
    println("ended ...")

  }

}

