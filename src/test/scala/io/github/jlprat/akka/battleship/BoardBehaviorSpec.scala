package io.github.jlprat.akka.battleship

import org.scalatest.{FlatSpec, Matchers}
import akka.actor.testkit.typed.scaladsl.TestInbox
import akka.actor.testkit.typed.scaladsl.BehaviorTestKit
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.testkit.typed.Effect

class BoardBehaviorSpecs extends FlatSpec with Matchers {

    "BoardBehavior" should "accept placing ships" in {
        val boardBehavior = BoardBehavior(3, 3)
        val testKit = BehaviorTestKit(boardBehavior)
        val initialCoordinate = Coordinate(1, 1)
        val replyTo = TestInbox[BoardBehavior.Protocol]()

        testKit.run(BoardBehavior.PlaceShip(2, initialCoordinate, Coordinate.Right, replyTo.ref))

        testKit.expectEffectType[Effect.SpawnedAnonymous[_]]

        replyTo.expectMessage(BoardBehavior.OK)
    }

    it should "fail to place a ship if any of the slots is already taken" in {

        val initialBoard = Board[ShipBehavior.Command](Seq.fill(3, 3)(None))
        val initialCoordinate = Coordinate(1, 1)
        val ship = TestInbox[ShipBehavior.Command]()
        val newBoard = initialBoard.take(initialCoordinate, ship.ref)

        val alreadyTakenPlacesBehavior = BoardBehavior.baseline(newBoard, 3)
        val testKit = BehaviorTestKit(alreadyTakenPlacesBehavior)

        val replyTo = TestInbox[BoardBehavior.Protocol]()

        testKit.run(BoardBehavior.PlaceShip(2, initialCoordinate, Coordinate.Right, replyTo.ref))
        testKit.returnedBehavior shouldBe Behaviors.same
        replyTo.expectMessage(BoardBehavior.KO)
    }


}
