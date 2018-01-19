package io.github.jlprat.akka

import akka.actor.typed.scaladsl.Actor
import akka.actor.typed.{ActorRef, Behavior}
import io.github.jlprat.akka.Guest.SitOnTable

object Maitre {

  sealed trait Command

  case class AskForTable(guest: ActorRef[Guest.Action]) extends Command
  case class TableAvailable(table: ActorRef[Table.Action]) extends Command
  case class OpenRestaurant(numberOfTables: Int) extends Command
  case object Foo

  def behavior: Behavior[Command] = init()

  def onDuty(availableTables: Seq[ActorRef[Table.Action]]): Behavior[Command] =
    Actor.immutable[Command] { (ctx, msg) =>
      msg match {
        case TableAvailable(table) =>
          ctx.system.log.info("New Table available")
          onDuty(table +: availableTables)
        case AskForTable(guest) if availableTables.nonEmpty =>
          // send people to a table
          val table = availableTables.head
          guest ! SitOnTable(table)
          onDuty(availableTables.tail)
        case _ =>
          // you should wait, for now we have a rude behavior and we ignore the guest
          Actor.unhandled
      }
    }

  def init(): Behavior[Command] = Actor.immutable[Command] { (ctx, msg) =>
      msg match {
        case OpenRestaurant(numberOfTables) if numberOfTables > 0 =>
          val tables = 0.until(numberOfTables).map { i =>
            ctx.system.log.info(s"creating child number $i")
            ctx.spawn(Table.initial, s"table-$i")
          }
          onDuty(tables)
        case _ =>
          println("foo")
          ctx.system.log.error("I don't understand you")
          Actor.unhandled
      }
  }
}
