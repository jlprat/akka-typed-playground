package io.github.jlprat.akka.battleship

import akka.actor.testkit.typed.scaladsl.BehaviorTestKit
import akka.actor.testkit.typed.CapturedLogEvent
import akka.actor.typed.scaladsl.Behaviors
import org.slf4j.event.Level
import org.scalatest.{FlatSpec, Matchers}
import akka.actor.testkit.typed.scaladsl.TestInbox

class ShipSpecs extends FlatSpec with Matchers {

    "A Ship" should "reply with NotYet when shoot is in any of the ship's coordinates" in {
        val placedShip = ShipBehavior.placedShip(Seq(Coordinate(1,3), Coordinate(1,4), Coordinate(1,5)))
        val testKit = BehaviorTestKit(placedShip)
        val handler = TestInbox[ShipBehavior.Response]()

        testKit.run(ShipBehavior.Shoot(Coordinate(1,3), handler.ref))
        testKit.returnedBehavior should not be Behaviors.same

        handler.expectMessage(ShipBehavior.NotYet)

        testKit.logEntries() shouldBe Seq(CapturedLogEvent(Level.INFO, "Ship shot at (1,3)"))
    }

    it should "reply with Sunk when shoot is the last of the ship's alive coordinates" in {
        val placedShip = ShipBehavior.placedShip(Seq(Coordinate(1,3)), Seq(Coordinate(1,4), Coordinate(1,5)))
        val testKit = BehaviorTestKit(placedShip)
        val handler = TestInbox[ShipBehavior.Response]()

        testKit.run(ShipBehavior.Shoot(Coordinate(1,3), handler.ref))
        testKit.returnedBehavior should not be Behaviors.same

        handler.expectMessage(ShipBehavior.Sunk)

        testKit.logEntries() shouldBe Seq(CapturedLogEvent(Level.INFO, "Ship shot and sunk at (1,3)"))
    }

    it should "reply with Wrong when shoot is not any of the ship's coordinates" in {
        val placedShip = ShipBehavior.placedShip(Seq(Coordinate(1,3), Coordinate(1,4), Coordinate(1,5)))
        val testKit = BehaviorTestKit(placedShip)
        val handler = TestInbox[ShipBehavior.Response]()

        testKit.run(ShipBehavior.Shoot(Coordinate(1,6), handler.ref))
        testKit.returnedBehavior shouldBe Behaviors.same

        handler.expectMessage(ShipBehavior.Wrong)

        testKit.logEntries() shouldBe Seq(CapturedLogEvent(Level.INFO, "Miss!"))
    }

    it should "reply with NotYet when shoot is already hit in that cooridinate" in {
        val placedShip = ShipBehavior.placedShip(Seq(Coordinate(1,3)), Seq(Coordinate(1,4), Coordinate(1,5)))
        val testKit = BehaviorTestKit(placedShip)
        val handler = TestInbox[ShipBehavior.Response]()

        testKit.run(ShipBehavior.Shoot(Coordinate(1,4), handler.ref))
        testKit.returnedBehavior shouldBe Behaviors.same

        handler.expectMessage(ShipBehavior.NotYet)

        testKit.logEntries() shouldBe Seq(CapturedLogEvent(Level.INFO, "Ship already shot at (1,4)"))
    }

    it should "reply with Sunk when shoot is any of ship's coordinates and already sunk" in {
        val placedShip = ShipBehavior.sunk(Seq(Coordinate(1,3), Coordinate(1,4), Coordinate(1,5)))
        val testKit = BehaviorTestKit(placedShip)
        val handler = TestInbox[ShipBehavior.Response]()

        testKit.run(ShipBehavior.Shoot(Coordinate(1,3), handler.ref))
        testKit.returnedBehavior shouldBe Behaviors.same

        handler.expectMessage(ShipBehavior.Sunk)

        testKit.logEntries() shouldBe Seq(CapturedLogEvent(Level.INFO, "Ship already sunk at (1,3)"))
    }

    it should "reply with Wrong when shoot is not any of ship's coordinates and already sunk" in {
        val placedShip = ShipBehavior.sunk(Seq(Coordinate(1,3), Coordinate(1,4), Coordinate(1,5)))
        val testKit = BehaviorTestKit(placedShip)
        val handler = TestInbox[ShipBehavior.Response]()

        testKit.run(ShipBehavior.Shoot(Coordinate(1,6), handler.ref))
        testKit.returnedBehavior shouldBe Behaviors.same

        handler.expectMessage(ShipBehavior.Wrong)

        testKit.logEntries() shouldBe Seq(CapturedLogEvent(Level.INFO, "Miss!"))
    }

}