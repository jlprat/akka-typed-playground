package io.github.jlprat.akka.alarm

import akka.actor.typed.{ActorSystem, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import io.github.jlprat.akka.alarm.Cell.Init

object Guard {

  sealed trait Command

  case class Start(children: Int) extends Command
  case class Alarm(coord: Int) extends Command
  case class SilenceSector(coord: Int) extends Command


  val initialBehavior: Behavior[Command] = Behaviors.receive{
    case (context, Start(numberOfChildren)) if numberOfChildren > 0 =>
      val cells = for {
        x <- 0 to numberOfChildren
      } yield {
        (x, context.spawn(Cell.init, s"Cell$x"))
      }

      cells.toList.tails.foreach {
        case first :: second :: _ => first._2 ! Init(Some(second._2), context.self, first._1)
        case only :: Nil => only._2 ! Init(None, context.self, only._1)
        case _ => context.log.info("foo")
      }
      listening
    case (context, Start(numberOfChildren)) if numberOfChildren == 0 =>
      context.log.error("Specify more than 0 children")
      Behaviors.same
    case (context, _) =>
      context.log.error("Behaviour should be started first!")
      Behaviors.unhandled
  }

  val listening: Behavior[Command] = Behaviors.receive {
    case (context, Alarm(coord)) =>
      context.log.info(s"Alarm in sector $coord")
      alarmed(Seq(coord))
    case _ => Behaviors.unhandled
  }


  def alarmed(sectors: Seq[Int]): Behavior[Command] = Behaviors.receive {
    case (context, SilenceSector(coord)) =>
      context.log.info(s"Alarm for sector $coord is now off")
      sectors.filterNot(_ == coord) match {
        case Nil => listening
        case x => alarmed(x)
      }
    case (context, Alarm(coord)) =>
      context.log.info(s"Alarm in sector $coord")
      alarmed(coord +: sectors)
    case _ => Behaviors.unhandled
  }

  def main(args: Array[String]): Unit = {
    val guardSystem: ActorSystem[Command] = ActorSystem(initialBehavior, "GuardSystem")
    guardSystem ! Start(4)

    Thread.sleep(60 * 1000)

    guardSystem.terminate()
  }

}
