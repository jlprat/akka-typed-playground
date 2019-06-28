package io.github.jlprat.akka.battleship

case class Coordinate(x:Int, y: Int) {

    def show: String = {
        s"($x,$y)"
    }
}
