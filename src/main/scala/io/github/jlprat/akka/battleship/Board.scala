package io.github.jlprat.akka.battleship
import akka.actor.typed.ActorRef


object Board {

    sealed trait State[+T] 
    case object Free extends State[Nothing]
    case class Taken[T](ref: ActorRef[T]) extends State[T]
      
    def apply(width: Int, height: Int) = new Board(Seq.fill(width, height)(Free))
    
}

case class Board[T] private (inner: Seq[Seq[Board.State[T]]]) {

    def apply(coordinate: Coordinate): Board.State[T] = inner(coordinate.x - 1)(coordinate.y - 1)

    def take(coordinate: Coordinate, ref: ActorRef[T]): Board[T] = {
        Board(inner.updated(coordinate.x - 1, inner(coordinate.x - 1).updated(coordinate.y - 1, Board.Taken(ref))))
    }
}

