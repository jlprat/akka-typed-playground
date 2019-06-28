package io.github.jlprat.akka.battleship

import org.scalatest.{FlatSpec, Matchers}

class BoardSpec extends FlatSpec with Matchers {


    "Board" should "mark cells as free by default" in {
        val width = 3
        val height = 3
        val board = Board(width, height)
        val coords = for {
            x <- 1 to width
            y <- 1 to height
        } yield Coordinate(x, y)

        coords.foreach(coord => 
            board(coord) shouldBe Board.Free
        )
    }
    
    it should "mark cells as taken if they are free" in {
        val board = Board(3,3)
        val newBoard = board.take(Coordinate(1,1))
        newBoard(Coordinate(1,1)) shouldBe Board.Taken
        newBoard(Coordinate(2,2)) shouldBe Board.Free
    }

}