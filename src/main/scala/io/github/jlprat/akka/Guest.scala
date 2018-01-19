package io.github.jlprat.akka

import akka.actor.typed.ActorRef

object Guest {

  sealed trait Action

  case class SitOnTable(table: ActorRef[Table.Action]) extends Action

}
