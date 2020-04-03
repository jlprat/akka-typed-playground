package io.github.jlprat.akka.battleship


import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.Behavior
import akka.actor.typed.ActorRef

object BoardBehavior {

    sealed trait BoardCommand
    final case class PlaceShip(size: Int, coord: Coordinate, direction: Coordinate.Direction, replyTo: ActorRef[Protocol]) extends BoardCommand
    final case class Shoot(coord: Coordinate, replyTo: ActorRef[Protocol]) extends BoardCommand

    sealed trait Protocol
    case object OK extends Protocol
    case object KO extends Protocol
    case object Hit extends Protocol
    case object Miss extends Protocol
    case object Sunk extends Protocol

    // Ship should reply messages readable for Board
    // private final case class WrappedShipResponse(response: Backend.Response) extends Command

    val totalShips = 17

    def apply(width: Int, height: Int): Behavior[BoardBehavior.BoardCommand] =
        baseline(Board[ShipBehavior.Command](Seq.fill(width, height)(None)), totalShips)


    def baseline(board: Board[ShipBehavior.Command], shipsToPlace: Int): Behavior[BoardBehavior.BoardCommand] = Behaviors.receive {(context, message) => 
        message match {
            case PlaceShip(size, initCoord, direction, replyTo) => 
                val endCoord = initCoord.move(direction, size)
                val coords = Coordinate.expandCoordinates(initCoord, endCoord)

                if (coords.forall(coord => board(coord).isEmpty)) {
                    val ship = context.spawnAnonymous(ShipBehavior(coords))
                    val newBoard = coords.foldLeft(board)((board, coord) => board.take(coord, ship))
                    replyTo ! OK

                    if (shipsToPlace == 1) {
                        inGame(newBoard)
                    } else {
                        baseline(newBoard, shipsToPlace - 1)
                    }
                }
                else {
                    replyTo ! KO
                    Behaviors.same
                }

            case _ => 
                context.log.info(s"Wrong message while in state `baseline`! $message")
                Behaviors.same
        }
    }

    def inGame(board: Board[ShipBehavior.Command]): Behavior[BoardBehavior.BoardCommand] = Behaviors.receive { (_, message) =>
        message match {
            case Shoot(coord, replyTo) =>
                board(coord)
                replyTo.path
                // val cell: Board.State[ShipBehavior.Command] = board(coord)
                // cell match {
                //     case Board.Taken(ref: ActorRef[ShipBehavior.Command]) =>
                //         ref ! ShipBehavior.Shoot(coord, replyTo)
                //     case Board.Free => replyTo ! Protocol.Miss
                // }
                Behaviors.same
            case _ => 
                Behaviors.same
        }
    }


}
