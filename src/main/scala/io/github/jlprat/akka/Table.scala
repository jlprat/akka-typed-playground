package io.github.jlprat.akka

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Actor

object Table {

  sealed trait Action
  case object Occupied extends Action

  def initial: Behavior[Action] = Actor.immutable[Action] { (ctx, msg) =>
    msg match {
      case _ =>
        Actor.empty
    }
  }

}
