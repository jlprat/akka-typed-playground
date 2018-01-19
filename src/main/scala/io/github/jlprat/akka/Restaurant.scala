package io.github.jlprat.akka

import akka.NotUsed
import akka.actor.typed.scaladsl.Actor
import akka.actor.typed.{ActorSystem, Behavior}

import scala.concurrent.Await
import scala.concurrent.duration._

object Restaurant extends App {

  val numberOfTables = 20

  val main: Behavior[NotUsed] = {
    Actor.deferred { ctx ⇒
//      val chatRoom = ctx.spawn(ChatRoom.behavior, "chatroom")
//      val gabblerRef = ctx.spawn(gabbler, "gabbler")
//      ctx.watch(gabblerRef)
//      chatRoom ! GetSession("ol’ Gabbler", gabblerRef)
//
//      Actor.immutable[akka.NotUsed] {
//        (_, _) ⇒ Actor.unhandled
//      } onSignal {
//        case (ctx, Terminated(ref)) ⇒
//          Actor.stopped
//      }
      Actor.ignore
    }
  }

  val system = ActorSystem(main, "RestaurantDemo")
  Await.result(system.whenTerminated, 3.seconds)
}
