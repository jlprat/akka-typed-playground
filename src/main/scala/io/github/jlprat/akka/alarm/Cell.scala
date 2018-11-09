package io.github.jlprat.akka.alarm

import java.time.LocalTime

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import io.github.jlprat.akka.alarm.Guard.{Alarm, Command, SilenceSector}

import scala.concurrent.duration._

object Cell {

  case object TimerKey

  sealed trait Action

  case class Init(next: Option[ActorRef[Action]], parent: ActorRef[Command], coord: Int) extends Action
  case object Warn extends Action
  case object ClearWarning extends Action
  case object CheckIntrusion extends Action

  private def checkIntrusion(coord: Int): Boolean = {
    LocalTime.now().getSecond % (coord + 2) == 0
  }

  val init: Behavior[Action] = Behaviors.withTimers { timers =>
    Behaviors.receiveMessage {
      case Init(ref, parent, coord) =>
        timers.startPeriodicTimer(TimerKey, CheckIntrusion, 100.millis)
        guarding(ref, parent, coord)
      case _ =>
        Behaviors.unhandled
    }
  }

  def guarding(toNotify: Option[ActorRef[Action]], parent: ActorRef[Command], coord: Int): Behavior[Action] = Behaviors.receive {
    case (context, CheckIntrusion) =>
      if (checkIntrusion(coord)) {
        context.log.info("Intruded Detected! Warn!")
        toNotify.foreach(_ ! Warn)
        intruded(toNotify, parent, coord)
      }
      else {
        Behaviors.same
      }
    case (context, Warn) =>
      context.log.info("Got warned by neighbour")
      warned(toNotify, parent, coord)
    case (context, msg) =>
      context.log.error(s"(Guarding) Unexpected message! $msg")
      Behaviors.same
  }

  def warned(toNotify: Option[ActorRef[Action]], parent: ActorRef[Command], coord: Int): Behavior[Action] = Behaviors.receive {
    case (context, CheckIntrusion) =>
      if (checkIntrusion(coord)) {
        context.log.info("Intruded Detected! Alarm!")
        toNotify.foreach(_ ! Warn)
        parent ! Alarm(coord)
        alarmed(toNotify, parent, coord)
      } else
        Behaviors.same
    case (context, ClearWarning) =>
      context.log.info("Cleaning warning")
      guarding(toNotify, parent, coord)
    case (_, Warn) =>
      Behaviors.same
    case (context, msg) =>
      context.log.error(s"(Warned) Unexpected message $msg")
      Behaviors.same
  }

  def intruded(toNotify: Option[ActorRef[Action]], parent: ActorRef[Command], coord: Int): Behavior[Action] = Behaviors.receive {
    case (context, Warn) =>
      context.log.info("Intruded Detected! Alarm!")
      parent ! Alarm(coord)
      alarmed(toNotify, parent, coord)
    case (context, CheckIntrusion) =>
      if (checkIntrusion(coord)) {
        Behaviors.same
      }
      else {
        context.log.info("Clear warning for neighbour!")
        toNotify.foreach(_ ! ClearWarning)
        guarding(toNotify, parent, coord)
      }
    case (context, msg) =>
      context.log.error(s"(Intruded) Unexpected message $msg")
      Behaviors.same
  }

  def alarmed(toNotify: Option[ActorRef[Action]], parent: ActorRef[Command], coord: Int): Behavior[Action] = Behaviors.receive {
    case (context, CheckIntrusion) =>
      if (checkIntrusion(coord)) {
        Behaviors.same
      }
      else {
        context.log.info("Intruder gone!")
        toNotify.foreach(_ ! ClearWarning)
        parent ! SilenceSector(coord)
        warned(toNotify, parent, coord)
      }
    case (context, ClearWarning) =>
      context.log.info("Warning gone!")
      parent ! SilenceSector(coord)
      intruded(toNotify, parent, coord)
    case (context, msg) =>
      context.log.error(s"(Alarmed) Unexpected message $msg")
      Behaviors.same
  }

}
