package io.github.jlprat.akka.battleship


object Board {

    sealed trait State
    case object Taken extends State
    case object Free extends State
      
    def apply(width: Int, height: Int) = new Board(Seq.fill(width, height)(Free))
    
}

case class Board private (inner: Seq[Seq[Board.State]]) {

    def apply(coordinate: Coordinate): Board.State = inner(coordinate.x - 1)(coordinate.y - 1)

    def take(coordinate: Coordinate): Board = {
        Board(inner.updated(coordinate.x - 1, inner(coordinate.x - 1).updated(coordinate.y - 1, Board.Taken)))
    }
}

