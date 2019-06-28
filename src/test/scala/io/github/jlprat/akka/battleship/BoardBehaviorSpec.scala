package io.github.jlprat.akka.battleship

import org.scalatest.{FlatSpec, Matchers}
import akka.actor.testkit.typed.scaladsl.TestInbox
import akka.actor.testkit.typed.scaladsl.BehaviorTestKit
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.testkit.typed.CapturedLogEvent
import akka.event.Logging


class BoardBehaviorSpecs extends FlatSpec with Matchers {

    "BoardBehavior" should "take a sequence of free slots" in {
        val boardBehavior = BoardBehavior(3, 3)
        val testKit = BehaviorTestKit(boardBehavior)
        val coords = Seq(Coordinate(1, 1), Coordinate(1, 2))
        val replyTo = TestInbox[BoardBehavior.BoardReply]()

        testKit.run(BoardBehavior.Take(coords, replyTo.ref))

        testKit.returnedBehavior shouldBe Behaviors.same
        testKit.logEntries() shouldBe Seq(CapturedLogEvent(Logging.InfoLevel, "Up for takes (1,1),(1,2)"))

        replyTo.expectMessage(BoardBehavior.OK)
    }

    it should "fail to take a sequence of slots if any of them is already taken" in {

        val boardBehavior = BoardBehavior(3, 3)
        val testKit = BehaviorTestKit(boardBehavior)
        val coords = Seq(Coordinate(1, 1), Coordinate(1, 2))
        val replyTo = TestInbox[BoardBehavior.BoardReply]()

        testKit.run(BoardBehavior.Take(coords, replyTo.ref))
        replyTo.expectMessage(BoardBehavior.OK)

        testKit.run(BoardBehavior.Take(coords, replyTo.ref))
        replyTo.expectMessage(BoardBehavior.KO)

        testKit.logEntries() shouldBe Seq(CapturedLogEvent(Logging.InfoLevel, "Up for takes (1,1),(1,2)"), CapturedLogEvent(Logging.InfoLevel, "Already occupied"))
    }
}
