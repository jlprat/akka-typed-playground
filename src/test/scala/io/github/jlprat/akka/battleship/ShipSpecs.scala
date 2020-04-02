package io.github.jlprat.akka.battleship

import akka.actor.testkit.typed.scaladsl.BehaviorTestKit
import akka.actor.testkit.typed.CapturedLogEvent
import akka.actor.typed.scaladsl.Behaviors
import org.slf4j.event.Level
import org.scalatest.{FlatSpec, Matchers}
import akka.actor.testkit.typed.scaladsl.TestInbox

class ShipSpecs extends FlatSpec with Matchers {

    // "A ship" should "be placed first before receiving shoots" in {
    //     val unplacedShipBehavior = ShipBehavior.unplacedShip(3)
    //     val testKit = BehaviorTestKit(unplacedShipBehavior)
    //     val handler = TestInbox[ShipBehavior.CommandReaction]()

    //     testKit.run(ShipBehavior.Shoot(Coordinate(1, 3), handler.ref))
    //     testKit.returnedBehavior shouldBe Behaviors.same

    //     handler.expectMessage(ShipBehavior.WrongCommand)

    //     testKit.logEntries() shouldBe Seq(CapturedLogEvent(Level.INFO, "Unexpected message!"))
    // }

    // it should "be placed" in {
    //     val unplacedShipBehavior = ShipBehavior.unplacedShip(3)
    //     val testKit = BehaviorTestKit(unplacedShipBehavior)
    //     val handler = TestInbox[ShipBehavior.CommandReaction]()

    //     testKit.run(ShipBehavior.Place(Coordinate(1, 3), Coordinate(1, 5), handler.ref))
    //     testKit.returnedBehavior should not be Behaviors.same

    //     handler.expectMessage(ShipBehavior.Placed)

    //     testKit.logEntries() shouldBe Seq(CapturedLogEvent(Level.INFO, "Placed Ship (1,3) - (1,5)"))
    // }

    // it should "be refuse to be placed is place is too big" in {
    //     val unplacedShipBehavior = ShipBehavior.unplacedShip(3)
    //     val testKit = BehaviorTestKit(unplacedShipBehavior)
    //     val handler = TestInbox[ShipBehavior.CommandReaction]()

    //     testKit.run(ShipBehavior.Place(Coordinate(1, 3), Coordinate(1, 6), handler.ref))
    //     testKit.returnedBehavior shouldBe Behaviors.same

    //     handler.expectMessage(ShipBehavior.NotFit)

    //     testKit.logEntries() shouldBe Seq(CapturedLogEvent(Level.INFO, "Ship of size 3 can't be placed in (1,3) - (1,6)"))
    // }

    // it should "be refuse to be placed is place is too small" in {
    //     val unplacedShipBehavior = ShipBehavior.unplacedShip(3)
    //     val testKit = BehaviorTestKit(unplacedShipBehavior)
    //     val handler = TestInbox[ShipBehavior.CommandReaction]()

    //     testKit.run(ShipBehavior.Place(Coordinate(1, 3), Coordinate(1, 4), handler.ref))
    //     testKit.returnedBehavior shouldBe Behaviors.same
        
    //     handler.expectMessage(ShipBehavior.NotFit)
        
    //     testKit.logEntries() shouldBe Seq(CapturedLogEvent(Level.INFO, "Ship of size 3 can't be placed in (1,3) - (1,4)"))
    // }

    "A Ship" should "reply with hit when shoot is in any of the ship's coordinates" in {
        val placedShip = ShipBehavior.placedShip(Seq(Coordinate(1,3), Coordinate(1,4), Coordinate(1,5)))
        val testKit = BehaviorTestKit(placedShip)
        val handler = TestInbox[ShipBehavior.CommandReaction]()

        testKit.run(ShipBehavior.Shoot(Coordinate(1,3), handler.ref))
        testKit.returnedBehavior should not be Behaviors.same

        handler.expectMessage(ShipBehavior.Hit)

        testKit.logEntries() shouldBe Seq(CapturedLogEvent(Level.INFO, "Ship shot at (1,3)"))
    }

    it should "reply with sunk when shoot is the last of the ship's alive coordinates" in {
        val placedShip = ShipBehavior.placedShip(Seq(Coordinate(1,3)), Seq(Coordinate(1,4), Coordinate(1,5)))
        val testKit = BehaviorTestKit(placedShip)
        val handler = TestInbox[ShipBehavior.CommandReaction]()

        testKit.run(ShipBehavior.Shoot(Coordinate(1,3), handler.ref))
        testKit.returnedBehavior should not be Behaviors.same

        handler.expectMessage(ShipBehavior.Sunk)

        testKit.logEntries() shouldBe Seq(CapturedLogEvent(Level.INFO, "Ship shot and sunk at (1,3)"))
    }

    it should "reply with miss when shoot is not any of the ship's coordinates" in {
        val placedShip = ShipBehavior.placedShip(Seq(Coordinate(1,3), Coordinate(1,4), Coordinate(1,5)))
        val testKit = BehaviorTestKit(placedShip)
        val handler = TestInbox[ShipBehavior.CommandReaction]()

        testKit.run(ShipBehavior.Shoot(Coordinate(1,6), handler.ref))
        testKit.returnedBehavior shouldBe Behaviors.same

        handler.expectMessage(ShipBehavior.Miss)

        testKit.logEntries() shouldBe Seq(CapturedLogEvent(Level.INFO, "Miss!"))
    }

    it should "reply with hit when shoot is already hit in that cooridinate" in {
        val placedShip = ShipBehavior.placedShip(Seq(Coordinate(1,3)), Seq(Coordinate(1,4), Coordinate(1,5)))
        val testKit = BehaviorTestKit(placedShip)
        val handler = TestInbox[ShipBehavior.CommandReaction]()

        testKit.run(ShipBehavior.Shoot(Coordinate(1,4), handler.ref))
        testKit.returnedBehavior shouldBe Behaviors.same

        handler.expectMessage(ShipBehavior.Hit)

        testKit.logEntries() shouldBe Seq(CapturedLogEvent(Level.INFO, "Ship already shot at (1,4)"))
    }

    it should "reply with sunk when shoot is any of ship's coordinates and already sunk" in {
        val placedShip = ShipBehavior.sunk(Seq(Coordinate(1,3), Coordinate(1,4), Coordinate(1,5)))
        val testKit = BehaviorTestKit(placedShip)
        val handler = TestInbox[ShipBehavior.CommandReaction]()

        testKit.run(ShipBehavior.Shoot(Coordinate(1,3), handler.ref))
        testKit.returnedBehavior shouldBe Behaviors.same

        handler.expectMessage(ShipBehavior.Sunk)

        testKit.logEntries() shouldBe Seq(CapturedLogEvent(Level.INFO, "Ship already sunk at (1,3)"))
    }

    it should "reply with mis when shoot is any of ship's coordinates and already sunk" in {
        val placedShip = ShipBehavior.sunk(Seq(Coordinate(1,3), Coordinate(1,4), Coordinate(1,5)))
        val testKit = BehaviorTestKit(placedShip)
        val handler = TestInbox[ShipBehavior.CommandReaction]()

        testKit.run(ShipBehavior.Shoot(Coordinate(1,6), handler.ref))
        testKit.returnedBehavior shouldBe Behaviors.same

        handler.expectMessage(ShipBehavior.Miss)

        testKit.logEntries() shouldBe Seq(CapturedLogEvent(Level.INFO, "Miss!"))
    }

}