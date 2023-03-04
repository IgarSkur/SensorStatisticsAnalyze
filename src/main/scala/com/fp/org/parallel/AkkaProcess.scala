package com.fp.org.parallel

object AkkaProcess extends App {

  import scala.annotation.tailrec
  import akka.actor.Actor
  import akka.actor.ActorRef
  import akka.actor.ActorSystem
  import akka.actor.Props
  import akka.actor._
  import akka.pattern.ask
  import akka.util.Timeout
  import scala.collection._
  import java.util.concurrent.atomic.AtomicLong
  import scala.concurrent.duration._
  import scala.language.postfixOps
  import java.util.concurrent.locks.{ReadWriteLock, ReentrantReadWriteLock}

  var batchsize: Int = 1
  var workerspool: Int = 4
  var averagepacket: Int = 0
  var source_main = true

  val eventmassive = new scala.collection.concurrent.TrieMap[Long, (String, String)]
  val actrorsTree = collection.mutable.HashMap.empty[Int, ActorRef]
  val lockW: ReadWriteLock = new ReentrantReadWriteLock()

  @volatile var iterator: BigInt = 0
  @volatile var invokes: Long = 0

  var acc: BigInt = 0
  var stop: Boolean = false
  var workersstarted: Boolean = true
  var wrkers: Int = 8
  var balance: Int = 5
  var calculusfor = 0

  case class Job(id: Long, input: Long, replyTo: ActorRef)
  case class JobResult(id: Long, acc: BigInt)
  case class JobIncomeMessage(query: String, fact: Int)
  case class JobData(el: (String, String))
  case class GetMessage()
  case class StopWorker()
  case class GetWork(work: ActorRef)
  case class TakeWorkers(wrk: Int)
  case class TakeBucket(nmbr: Int)
  case class WorkRequest(worker: ActorRef, items: Int)
  case class Finish()

  class Manager extends Actor {

    var outstandingWork = 0
    def receive = {
      case TakeWorkers(n) =>
        wrkers = n
        system.log.warning(s"workers initialization = ${n} ")
      case TakeBucket(n) =>
        balance = n
        system.log.warning(s"bucket initialization = ${n} ")
      case JobData(el) =>
        val packet = DataManagements(0L)
        packet.insert(el)
        sender ! "success"
      case StopWorker =>
        stop = true
      case JobIncomeMessage(query, n) =>
        calculusfor = n
        var i = 0
        while (i<wrkers) {
          i = i + 1
          val actor = context.system.actorOf(Props(new Worker(i.toLong, self)))
          system.log.warning(s"worker start : ${i.toLong} as worker ${actor.hashCode()}")
          actrorsTree += (actor.hashCode() -> actor)
        }
        sender ! "success"
      case GetMessage() => {
        system.log.warning(s"works been for ${acc} items by ${wrkers} workers, actors ${actrorsTree.size}")

        for ((id, actor) <- actrorsTree) {
          system.log.warning(s"actor ${actor.hashCode()} stoped")
          context.system.stop(actor)
        }

        system.log.warning(s"actors cleared")

        context.system.terminate()
        sender ! acc.toString()
      }
      case Finish() =>
        eventmassive.clear();  actrorsTree.clear()
      case WorkRequest(worker, items) =>
        def follow(items: Long) {
          var iterator :Long = 0
          @tailrec
          def cyclic(data: => Boolean)(cond : =>Boolean)(work : =>Boolean) : Unit =
            if(data && cond) {
              work
              cyclic(data)(cond)(work)
            }

          cyclic {eventmassive.size > 0} {(iterator < items)} {
            iterator = iterator + 1
            val job = Job(invokes, invokes, self)
            worker ! job
            outstandingWork += 1
            Thread.sleep (10)
            (eventmassive.size > 0)
          }
        }

        follow (items)

      case JobResult(id, report) =>
        outstandingWork -= 1
        if (outstandingWork == 0 || stop) {
          system.log.warning(s"finishing ... ")
          acc = report
          eventmassive.clear()
        }
    }
  }

  case class DataManagements(id: Long) {
    def isContent(): Boolean = {
      ((!eventmassive.isEmpty) && (eventmassive.contains(id)))
    }
    def delebyId() : Boolean = {
      if (isContent()) {
        val ers = eventmassive(id)
        eventmassive.remove(id, ers)
      } else false
    }
    def insert(el : (String, String)) = {
      iterator = iterator + 1
      system.log.warning(s"inserted id = ${iterator}")
      eventmassive.put (iterator.toLong, el )
      iterator
    }
  }

  class Worker(work: Long, manager: ActorRef) extends Actor {
    var StreamOff = false
    var requested = 0

    def receive =  {
      case Job(id, data, replyTo) =>
        requested -= 1
        cyclingbyPhase(id.toInt, replyTo)
        getter()
    }


    def request(): Unit =
      if (requested < 2) {
        manager ! WorkRequest(self, balance)
        requested += balance
      }

    def getter() {
      @tailrec
      def cyclic(data: => Boolean)(work : =>Boolean) : Unit =
        if(data) {
          work
          cyclic(data)(work)
        }

      cyclic {eventmassive.size == 0 && !stop} {
        manager ! WorkRequest(self, balance)
        Thread.sleep (10)
        (eventmassive.size == 0 && !stop)
      }

      request()
    }

    getter ()

    def packetBuildItems(id: Long, task: Long, replyTo: ActorRef): Unit = {
      val file = eventmassive(id)
      eventmassive.remove(id, eventmassive(id))
      system.log.warning(s"TO DO IT for ${file._1}, make task by id = ${id}, task = ${task}, HASH = ${replyTo}")
      lockW.writeLock().lock()
      StatisticsAnalyzeProcess.startStx(file._1)
      lockW.writeLock().unlock()
    }

    implicit class ToSortedMap[A,B](tuples: TraversableOnce[(A, B)])
                                   (implicit ordering: Ordering[A]) {
      def toSortedMap =
        SortedMap(tuples.toSeq: _*)
    }


    def cyclingbyPhase(task: Int, replyTo: ActorRef): Unit = {

      invokes = invokes + 1
      val isdatain = eventmassive.contains(invokes)
      if (!isdatain) {
        invokes = invokes - 1
      }

      packetBuildItems(invokes, invokes, replyTo)

    }

  }

  implicit val timeout = Timeout (5 seconds)

  implicit val system = ActorSystem ()
  val toActor = system.actorOf (Props[Manager], name = "toActor")
  actrorsTree += (toActor.hashCode() -> toActor)

  val lstjsonsl = List(
    s"""leader-1.csv""",
    s"""leader-2.csv"""
  )

  toActor ? TakeBucket (batchsize)
  toActor ? TakeWorkers (workerspool)
  toActor ? JobIncomeMessage ("hash", averagepacket)

  println(s"load data size by packet, items = ${lstjsonsl}")
  lstjsonsl.foreach { a => toActor ? JobData((a,null))}

  Thread.sleep (1000)
  while (eventmassive.size>0) {}

  toActor ! StopWorker
  val future = toActor ? GetMessage ()
  Thread.sleep (1000)
  system.log.warning(s"workers closed = ${future} ")

  Thread.sleep (1000)

  toActor ! Finish

  Thread.sleep (1000)

  system.stop(toActor)

  system.terminate()
  system.log.warning(s"finisned ok")

}

/**
 *  USED 4 workers, actors 5, Actor invoke Async Cats Effects and Async Collections
 *  each manager got result (print)
 *
[WARN] [xx/xx/xxxx 14:56:38.067] [default-akka.actor.default-dispatcher-6] [akka.actor.ActorSystemImpl(default)] bucket initialization = 1 
load data size by packet, items = List(leader-1.csv, leader-2.csv)
[WARN] [xx/xx/xxxx 14:56:38.067] [default-akka.actor.default-dispatcher-6] [akka.actor.ActorSystemImpl(default)] workers initialization = 4 
[WARN] [xx/xx/xxxx 14:56:38.073] [default-akka.actor.default-dispatcher-6] [akka.actor.ActorSystemImpl(default)] worker start : 1 as worker -1674895691
[WARN] [xx/xx/xxxx 14:56:38.073] [default-akka.actor.default-dispatcher-6] [akka.actor.ActorSystemImpl(default)] worker start : 2 as worker -911862501
[WARN] [xx/xx/xxxx 14:56:38.074] [default-akka.actor.default-dispatcher-6] [akka.actor.ActorSystemImpl(default)] worker start : 3 as worker 1847872003
[WARN] [xx/xx/xxxx 14:56:38.074] [default-akka.actor.default-dispatcher-6] [akka.actor.ActorSystemImpl(default)] worker start : 4 as worker -801036214
[WARN] [xx/xx/xxxx 14:56:38.080] [default-akka.actor.default-dispatcher-11] [akka.actor.ActorSystemImpl(default)] inserted id = 1
[WARN] [xx/xx/xxxx 14:56:38.082] [default-akka.actor.default-dispatcher-11] [akka.actor.ActorSystemImpl(default)] inserted id = 2
[WARN] [xx/xx/xxxx 14:56:38.100] [default-akka.actor.default-dispatcher-5] [akka.actor.ActorSystemImpl(default)] TO DO IT for leader-1.csv, make task by id = 1, task = 1, HASH = Actor[akka://default/user/toActor#-2007206382]
 1) ============================================
 sensor-id s1; humidity : 10 
 sensor-id s2; humidity : 88 
 sensor-id s1; humidity : NaN 
 sensor-id s1; humidity : 20 
 item: : s1-NaN
Task: key: s1 contains: false value: 0
Task: key: s1 contains: true value: 10
Task: key: s2 contains: false value: 88
Task: key: s1 contains: true value: 20
[WARN] [xx/xx/xxxx 14:56:38.197] [default-akka.actor.default-dispatcher-8] [akka.actor.ActorSystemImpl(default)] TO DO IT for leader-2.csv, make task by id = 2, task = 2, HASH = Actor[akka://default/user/toActor#-2007206382]
ended ...
 Sensors with highest avg humidity: 
sensor-id: s1
min: 0 max: 20 min: 0 sum: 30
sensor-id: s2
min: 0 max: 88 min: 0 sum: 88
 1) ============================================
 sensor-id s2; humidity : 80 
 sensor-id s3; humidity : NaN 
 sensor-id s2; humidity : 78 
 sensor-id s1; humidity : 98 
 item: : s3-NaN
Task: key: s2 contains: true value: 80
Task: key: s2 contains: true value: 78
Task: key: s1 contains: true value: 98
Task: key: s3 contains: false value: 0
ended ...
 Sensors with highest avg humidity: 
sensor-id: s3
min: 0 max: 0 min: 0 sum: 0
sensor-id: s1
min: 0 max: 98 min: 0 sum: 98
sensor-id: s2
min: 0 max: 80 min: 0 sum: 158
[WARN] [xx/xx/xxxx 14:56:40.085] [default-akka.actor.default-dispatcher-11] [akka.actor.ActorSystemImpl(default)] works been for 0 items by 4 workers, actors 5
[WARN] [xx/xx/xxxx 14:56:40.086] [default-akka.actor.default-dispatcher-11] [akka.actor.ActorSystemImpl(default)] actor 1847872003 stoped
[WARN] [xx/xx/xxxx 14:56:40.087] [default-akka.actor.default-dispatcher-11] [akka.actor.ActorSystemImpl(default)] actor -801036214 stoped
[WARN] [xx/xx/xxxx 14:56:40.088] [default-akka.actor.default-dispatcher-11] [akka.actor.ActorSystemImpl(default)] actor -911862501 stoped
[WARN] [xx/xx/xxxx 14:56:40.088] [default-akka.actor.default-dispatcher-11] [akka.actor.ActorSystemImpl(default)] actor -2007206382 stoped
[WARN] [xx/xx/xxxx 14:56:40.088] [default-akka.actor.default-dispatcher-11] [akka.actor.ActorSystemImpl(default)] actor -1674895691 stoped
[WARN] [xx/xx/xxxx 14:56:40.088] [default-akka.actor.default-dispatcher-11] [akka.actor.ActorSystemImpl(default)] actors cleared
[INFO] [akkaDeadLetter][xx/xx/xxxx 14:56:40.113] [default-akka.actor.default-dispatcher-8] [akka://default/user/toActor] Message [com.fp.org.parallel.AkkaProcess$WorkRequest] from Actor[akka://default/user/$a#-1674895691] to Actor[akka://default/user/toActor#-2007206382] was not delivered. [1] dead letters encountered. If this is not an expected behavior then Actor[akka://default/user/toActor#-2007206382] may have terminated unexpectedly. This logging can be turned off or adjusted with configuration settings 'akka.log-dead-letters' and 'akka.log-dead-letters-during-shutdown'.
[WARN] [xx/xx/xxxx 14:56:45.093] [main] [akka.actor.ActorSystemImpl(default)] workers closed = Future(Success(0)) 
[WARN] [xx/xx/xxxx 14:56:53.098] [main] [akka.actor.ActorSystemImpl(default)] finisned ok
 */
