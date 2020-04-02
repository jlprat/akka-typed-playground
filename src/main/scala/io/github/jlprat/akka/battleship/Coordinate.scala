package io.github.jlprat.akka.battleship
import io.github.jlprat.akka.battleship.Coordinate.Direction
import io.github.jlprat.akka.battleship.Coordinate.Down

object Coordinate { 
    sealed trait Direction
    case object Right extends Direction
    case object Down extends Direction


    def expandCoordinates(begin: Coordinate, end: Coordinate): Seq[Coordinate] = (begin,end) match {
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
}
case class Coordinate(x:Int, y: Int) {

    def show: String = {
        s"($x,$y)"
    }

    def move(direction: Direction, steps: Int): Coordinate = direction match {
        case Down => Coordinate(x, y + steps)
        case _ =>  Coordinate(x + steps, y)
    }

}
