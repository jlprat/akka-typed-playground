package io.github.jlprat.akka.battleship
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ ActorRef, Behavior }


object Ship {
    sealed trait Command
    case class Shoot(coordinate: Coordinate, handler: ActorRef[CommandReaction]) extends Command
    case class Place(begin: Coordinate, end: Coordinate, handler: ActorRef[CommandReaction]) extends Command

    sealed trait CommandReaction
    case object WrongCommand extends CommandReaction
    case object Hit extends CommandReaction
    case object Miss extends CommandReaction
    case object Sunk extends CommandReaction
    case object NotFit extends CommandReaction
    case object Placed extends CommandReaction


    def isFitting(size: Int, begin: Coordinate, end: Coordinate): Boolean = (begin,end) match {
        case (Coordinate(x1, y1), Coordinate(x2, y2)) if x1 == x2 => Math.max(y1, y2) - Math.min(y1, y2) == size -1
        case (Coordinate(x1, y1), Coordinate(x2, y2)) if y1 == y2 => Math.max(x1, x2) - Math.min(x1, x2) == size -1
        case _ => false
    }

    def shipCoordinates(size: Int, begin: Coordinate, end: Coordinate): Seq[Coordinate] = (begin,end) match {
            case (Coordinate(x1, y1), Coordinate(x2, y2)) if x1 == x2 => 
                for {
                    y <- Math.min(y1, y2) to Math.max(y1, y2)
                } yield Coordinate(x1, y)
            case (Coordinate(x1, y1), Coordinate(x2, y2)) if y1 == y2 => 
                for {
                    x <- Math.min(x1, x2) to Math.max(x1, x2)
                } yield Coordinate(x, y1)
            case _ => Seq.empty
    }


    def unplacedShip(size: Int): Behavior[Command] = Behaviors.receive { (context, message) =>
        message match {
            case Place(begin, end, handler) if isFitting(size, begin, end) => 
                context.log.info("Placed Ship {} - {}", begin.show, end.show)
                handler ! Placed
                placedShip(shipCoordinates(size, begin, end))
            case Place(begin, end, handler) =>
                context.log.info("Ship of size {} can't be placed in {} - {}", size, begin.show, end.show)
                handler ! NotFit
                Behaviors.same
            case Shoot(_, handler) =>
                context.log.info("Unexpected message!")
                handler ! WrongCommand
                Behaviors.same
        }

    }


    def placedShip(aliveCoords: Seq[Coordinate], hit: Seq[Coordinate] = Seq.empty): Behavior[Command] = Behaviors.receive { (context, message) => 
        message match {
            case Place(_, _ , handler) =>
                context.log.info("Unexpected message")
                handler ! WrongCommand 
                Behaviors.same
            case Shoot(coord, handler) if aliveCoords.contains(coord) && aliveCoords.size ==1 =>
                context.log.info("Ship shot and sunk at {}", coord.show)
                handler ! Sunk
                sunk(coord +: hit)
            case Shoot(coord, handler) if aliveCoords.contains(coord) =>
                context.log.info("Ship shot at {}", coord.show)
                handler ! Hit
                placedShip(aliveCoords.filterNot( _ == coord), coord +: hit)
            case Shoot(coord, handler) if hit.contains(coord) =>
                context.log.info("Ship already shot at {}", coord.show)
                handler ! Hit
                Behavior.same
            case Shoot(_, handler) =>
                context.log.info("Miss!")
                handler ! Miss
                Behavior.same
        }
    }


    def sunk(hit: Seq[Coordinate]): Behavior[Command] = Behaviors.receive{ (context, message) =>
        message match {
            case Place(_, _ , handler) =>
                context.log.info("Unexpected message")
                handler ! WrongCommand 
                Behaviors.same
            case Shoot(coord, handler) if hit.contains(coord) =>
                context.log.info("Ship already sunk at {}", coord.show)
                handler ! Sunk
                Behavior.same
            case Shoot(_, handler) =>
                context.log.info("Miss!")
                handler ! Miss
                Behavior.same
        }
    }
}
