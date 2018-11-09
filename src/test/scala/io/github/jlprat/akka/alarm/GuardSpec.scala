package io.github.jlprat.akka.alarm

import akka.actor.testkit.typed.Effect.Spawned
import akka.actor.testkit.typed.scaladsl.BehaviorTestKit
import akka.actor.typed.Behavior
import io.github.jlprat.akka.alarm.Cell.{Action, Init}
import io.github.jlprat.akka.alarm.Guard.{Alarm, SilenceSector, Start}
import org.scalatest.{FlatSpec, Matchers}

class GuardSpec extends FlatSpec with Matchers {

  "Guard initial Behavior" should "create children when Init is sent" in {
    val testKit = BehaviorTestKit(Guard.initialBehavior)
    testKit.run(Start(1))
    testKit.returnedBehavior shouldBe Guard.listening
    testKit.expectEffect(Spawned(Cell.init, "Cell0"))
    testKit.expectEffect(Spawned(Cell.init, "Cell1"))

    val firstChildInbox = testKit.childInbox[Action]("Cell0")
    val secondChildInbox = testKit.childInbox[Action]("Cell1")
    firstChildInbox.expectMessage(Init(Some(secondChildInbox.ref), testKit.ref, 0))
    secondChildInbox.expectMessage(Init(None, testKit.ref, 1))
  }

  it should "do nothing if Init with 0 is sent" in {
    val testKit = BehaviorTestKit(Guard.initialBehavior)
    testKit.run(Start(0))
    testKit.returnedBehavior shouldBe Behavior.same
  }

  "Guard listening Behavior" should "listen to alarms" in {
    val testKit = BehaviorTestKit(Guard.listening)
    testKit.run(Alarm(0))
    testKit.returnedBehavior should not be Behavior.unhandled

  }

  it should "ignore other messages" in {
    val testKit = BehaviorTestKit(Guard.listening)
    testKit.run(Start(0))
    testKit.returnedBehavior shouldBe Behavior.unhandled
    testKit.run(SilenceSector(0))
    testKit.returnedBehavior shouldBe Behavior.unhandled
  }

}
