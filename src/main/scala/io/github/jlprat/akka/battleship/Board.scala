package io.github.jlprat.akka.battleship
import akka.actor.typed.ActorRef


object Board {     

    def apply[T](width: Int, height: Int) = new Board[T](Seq.fill(width, height)(None))
    
}

case class Board[T] private (inner: Seq[Seq[Option[ActorRef[T]]]]) {

    //TODO input validation
    def apply(coordinate: Coordinate): Option[ActorRef[T]] = inner(coordinate.x - 1)(coordinate.y - 1)

    //TODO input validation
    def take(coordinate: Coordinate, ref: ActorRef[T]): Board[T] = {
        Board(inner.updated(coordinate.x - 1, inner(coordinate.x - 1).updated(coordinate.y - 1, Some(ref))))
    }
}

