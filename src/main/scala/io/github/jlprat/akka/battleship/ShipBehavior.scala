package io.github.jlprat.akka.battleship
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ ActorRef, Behavior }


object ShipBehavior {
 
    sealed trait Command
    case class Shoot(coordinate: Coordinate, handler: ActorRef[Response]) extends Command

    sealed trait Response
    case object Sunk extends Response
    case object NotYet extends Response
    case object Wrong extends Response

    // def init(begin: Coordinate, end: Coordinate): Behavior[ShipBehavior.Command] = placedShip(Coordinate.expandCoordinates(begin, end))

    def apply(coordinates: Seq[Coordinate]): Behavior[ShipBehavior.Command] = placedShip(coordinates)

    def placedShip(aliveCoords: Seq[Coordinate], hit: Seq[Coordinate] = Seq.empty): Behavior[ShipBehavior.Command] = Behaviors.receive { (context, message) => 
        message match {
            case Shoot(coord, handler) if aliveCoords.contains(coord) && aliveCoords.size ==1 =>
                context.log.info("Ship shot and sunk at {}", coord.show)
                handler ! Sunk
                sunk(coord +: hit)
            case Shoot(coord, handler) if aliveCoords.contains(coord) =>
                context.log.info("Ship shot at {}", coord.show)
                handler ! NotYet
                placedShip(aliveCoords.filterNot( _ == coord), coord +: hit)
            case Shoot(coord, handler) if hit.contains(coord) =>
                context.log.info("Ship already shot at {}", coord.show)
                handler ! NotYet
                Behaviors.same
            case Shoot(_, handler) =>
                context.log.info("Miss!")
                handler ! Wrong
                Behaviors.same
        }
    }


    def sunk(hit: Seq[Coordinate]): Behavior[ShipBehavior.Command] = Behaviors.receive{ (context, message) =>
        message match {
            case Shoot(coord, handler) if hit.contains(coord) =>
                context.log.info("Ship already sunk at {}", coord.show)
                handler ! Sunk
                Behaviors.same
            case Shoot(_, handler) =>
                context.log.info("Miss!")
                handler ! Wrong
                Behaviors.same
        }
    }
}
