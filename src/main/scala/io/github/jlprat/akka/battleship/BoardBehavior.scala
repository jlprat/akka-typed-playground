package io.github.jlprat.akka.battleship


import akka.actor.typed.scaladsl.AbstractBehavior
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.ActorContext
import akka.actor.typed.ActorRef

object BoardBehavior {
    final case class Take(coords: Seq[Coordinate], replyTo: ActorRef[BoardReply])

    sealed trait BoardReply
    case object OK extends BoardReply
    case object KO extends BoardReply

    def apply(width: Int, height: Int): Behavior[Take] = {
        Behaviors.setup(context => new BoardBehavior(context, width, height))
    }
}

class BoardBehavior(context: ActorContext[BoardBehavior.Take], width: Int, height: Int) extends AbstractBehavior[BoardBehavior.Take](context) {
    
    var board = Board(Seq.fill(width, height)(Board.Free))

    override def onMessage(msg: BoardBehavior.Take): Behavior[BoardBehavior.Take] = {

        msg match {
            case BoardBehavior.Take(coords, replyTo) if coords.forall(coord => board(coord) == Board.Free) =>
                context.log.info("Up for takes {}", coords.map(_.show).mkString(","))
                coords.foreach{ coord =>
                    board = board.take(coord)
                }
                replyTo ! BoardBehavior.OK
                Behaviors.same
            case BoardBehavior.Take(_, replyTo) => 
                context.log.info("Already occupied")
                replyTo ! BoardBehavior.KO
                Behaviors.same
        }
        
    }
}