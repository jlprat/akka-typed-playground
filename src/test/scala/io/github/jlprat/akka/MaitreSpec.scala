package io.github.jlprat.akka

import akka.actor.typed.Behavior
import akka.testkit.typed._
import io.github.jlprat.akka.Maitre.{AskForTable, OpenRestaurant, TableAvailable}
import org.scalatest.{FlatSpec, Matchers}

class MaitreSpec extends FlatSpec with Matchers {


  "Maitre init behavior" should "listen to Open Restaurant message and be on duty" in {
    val testKit = MyBehaviorTestkit(Maitre.init)

    testKit.runUncanonical(OpenRestaurant(2))

    val table1 = testKit.childInbox("table-0")

    testKit.expectEffect(Effect.Spawned(Table.initial, "table-0"))
    testKit.expectEffect(Effect.Spawned(Table.initial, "table-1"))

    testKit.currentBehavior should not be Behavior.same
    testKit.currentBehavior should not be Behavior.unhandled
    testKit.currentBehavior should not be Behavior.empty
    testKit.currentBehavior should not be Behavior.ignore
  }

  it should "ignore other messages" in {

    val testKit = MyBehaviorTestkit(Maitre.init)
    val guest = TestInbox[Guest.Action]("guest-1")

    testKit.runUncanonical(AskForTable(guest.ref))

    testKit.retrieveAllEffects() shouldBe Seq.empty

    Behavior.isUnhandled(testKit.currentBehavior) shouldBe true // fails because behavior is canonicalized. Using custom method works

  }

  "Maitre on duty behavior" should "listen to tables being freed" in {

    val testKit = MyBehaviorTestkit(Maitre.onDuty(Seq.empty))

    val newTable = TestInbox[Table.Action]("table-x")

    testKit.runUncanonical(TableAvailable(newTable.ref))

    testKit.retrieveAllEffects() shouldBe Seq.empty


    testKit.currentBehavior shouldBe Maitre.onDuty(Seq(newTable.ref)) // No real way to check behaviors`

  }

}
