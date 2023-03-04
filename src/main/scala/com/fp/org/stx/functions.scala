package com.fp.org.stx

object functions {

  import java.util.concurrent.locks.{ReadWriteLock, ReentrantReadWriteLock}

  var min_base: Int = 0
  var max_base: Int = 0
  var sum_base: Int = 0
  var avr_base: Int = 0
  val isPrintableProcess = false

  case class stx(min: Int, max: Int, sm: Int, avr: Int)

  trait TracePrint[F[_]] {
    def putStr(msg: String): F[Unit]
    def getStr: F[String]
  }

  object TracePrint {
    def apply[F[_]](implicit F: TracePrint[F]): TracePrint[F] = F
  }

  trait Calculator[F[_]] {
    def min(n: Int): F[Int]
    def max(n: Int): F[Int]
    def sum(n: Int): F[Int]
    def avr: F[Int]
  }

  object Calculator {
    def apply[F[_]](implicit F: Calculator[F]): Calculator[F] = F
  }

  trait Validation[F[_]] {
    def validateInt(str: String): F[Option[Int]]
  }

  object Validation {
    def apply[F[_]](implicit F: Validation[F]): Validation[F] = F
  }

  trait Monad[F[_]] {
    def pure[A](x: A): F[A]
    def flatMap[A, B](fa: F[A])(f: A => F[B]): F[B]
  }

  class IO[+A](val unsafeRun: () => A) { s =>
    def map[B](f: A => B): IO[B] = flatMap(f.andThen(IO(_)))
    def flatMap[B](f: A => IO[B]): IO[B] = IO(f(s.unsafeRun()).unsafeRun())
  }

  object IO {
    def apply[A](eff: => A): IO[A] = new IO(() => eff)
  }

  implicit class MOps[M[_], A](m: M[A])(implicit monad: Monad[M]) {
    def flatMap[B](f: A => M[B]): M[B] = monad.flatMap(m)(f)
    def map[B](f: A => B): M[B] = monad.flatMap(m)(f.andThen(monad.pure))
  }

  def product_min[F[_]: Monad: TracePrint: Calculator: Validation](n: Int): F[Unit] = {
    Calculator[F].min(n).flatMap(fact => { min_base = fact; TracePrint[F].putStr(s"min of $n is $fact")})
  }

  def product_max[F[_]: Monad: TracePrint: Calculator: Validation](n: Int): F[Unit] = {
    Calculator[F].max(n).flatMap(fact => { max_base = fact;TracePrint[F].putStr(s"max of $n is $fact")})
  }

  def product_sum[F[_]: Monad: TracePrint: Calculator: Validation](n: Int): F[Unit] = {
    Calculator[F].sum(n).flatMap(fact => { sum_base = fact;TracePrint[F].putStr(s"sum of $n is $fact")})
  }

  def product_avr[F[_]: Monad: TracePrint: Calculator: Validation](n: Int): F[Unit] = {
    Calculator[F].avr.flatMap(fact => { avr_base = fact;TracePrint[F].putStr(s"avr of $n is $fact")})
  }

  implicit val TracePrintIO: TracePrint[IO] = new TracePrint[IO] {
    def putStr(msg: String): IO[Unit] = IO(if (isPrintableProcess) println(msg))
    def getStr: IO[String] = IO(scala.io.StdIn.readLine())
  }

  implicit val validationIO: Validation[IO] = new Validation[IO] {
    import scala.util.Try
    def validateInt(str: String): IO[Option[Int]] =
      IO(Try(str.toInt).toOption)
  }

  implicit val calcIO: Calculator[IO] = new Calculator[IO] {
    def min(n: Int): IO[Int] = IO(if (min_base < n.asInstanceOf[Int]) min_base else n.asInstanceOf[Int])
    def max(n: Int): IO[Int] = IO(if (max_base > n.asInstanceOf[Int]) max_base else n.asInstanceOf[Int])
    def sum(n: Int): IO[Int] = IO(sum_base + n.asInstanceOf[Int])
    def avr: IO[Int] = IO(if (max_base!=0) sum_base / max_base else 0)
  }

  implicit val ioMonad: Monad[IO] = new Monad[IO] {
    def pure[A](x: A): IO[A] = IO(x)
    def flatMap[A, B](fa: IO[A])(f: A => IO[B]): IO[B] = fa.flatMap(f)
  }

  val event = collection.mutable.HashMap.empty[String, stx]
  val lockW: ReadWriteLock = new ReentrantReadWriteLock()

  def calc(n: Int) = {product_max[IO](n).unsafeRun(); product_min[IO](n).unsafeRun(); product_sum[IO](n).unsafeRun();product_avr[IO](n).unsafeRun()}
  def clear = {min_base = 0;max_base = 0;sum_base = 0;avr_base = 0; event.clear()}

}
