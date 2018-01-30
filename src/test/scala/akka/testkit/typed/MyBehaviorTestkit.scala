package akka.testkit.typed

import akka.actor.typed.{Behavior, PostStop, Signal}
import akka.annotation.ApiMayChange

import scala.annotation.tailrec
import scala.collection.immutable
import scala.util.control.Exception.Catcher
import scala.util.control.NonFatal

@ApiMayChange
object MyBehaviorTestkit {
  def apply[T](initialBehavior: Behavior[T], name: String): MyBehaviorTestkit[T] =
    new MyBehaviorTestkit[T](name, initialBehavior)
  def apply[T](initialBehavior: Behavior[T]): MyBehaviorTestkit[T] =
    apply(initialBehavior, "testkit")

  /**
    * JAVA API
    */
  def create[T](initialBehavior: Behavior[T], name: String): MyBehaviorTestkit[T] =
    new MyBehaviorTestkit[T](name, initialBehavior)
  /**
    * JAVA API
    */
  def create[T](initialBehavior: Behavior[T]): MyBehaviorTestkit[T] =
    apply(initialBehavior, "testkit")
}

/**
  * Used for testing [[Behavior]]s. Stores all effects e.g. Spawning of children,
  * watching and offers access to what effects have taken place.
  */
@ApiMayChange
class MyBehaviorTestkit[T] private(_name: String, _initialBehavior: Behavior[T]) {

  import Effect._

  // really this should be private, make so when we port out tests that need it
  private[akka] val ctx = new EffectfulActorContext[T](_name)

  /**
    * Requests the oldest [[Effect]] or [[NoEffects]] if no effects
    * have taken place. The effect is consumed, subsequent calls won't
    * will not include this effect.
    */
  def retrieveEffect(): Effect = ctx.effectQueue.poll() match {
    case null ⇒ NoEffects
    case x    ⇒ x
  }

  def childInbox[U](name: String): TestInbox[U] = {
    val inbox = ctx.childInbox[U](name)
    assert(inbox.isDefined, s"Child not created: $name. Children created: [${ctx.childrenNames.mkString(",")}]")
    inbox.get
  }

  def selfInbox(): TestInbox[T] = ctx.selfInbox

  /**
    * Requests all the effects. The effects are consumed, subsequent calls will only
    * see new effects.
    */
  def retrieveAllEffects(): immutable.Seq[Effect] = {
    @tailrec def rec(acc: List[Effect]): List[Effect] = ctx.effectQueue.poll() match {
      case null ⇒ acc.reverse
      case x    ⇒ rec(x :: acc)
    }

    rec(Nil)
  }

  /**
    * Asserts that the oldest effect is the expectedEffect. Removing it from
    * further assertions.
    */
  def expectEffect(expectedEffect: Effect): Unit = {
    ctx.effectQueue.poll() match {
      case null   ⇒ assert(assertion = false, s"expected: $expectedEffect but no effects were recorded")
      case effect ⇒ assert(expectedEffect == effect, s"expected: $expectedEffect but for $effect")
    }
  }

  private var current = Behavior.validateAsInitial(Behavior.undefer(_initialBehavior, ctx))

  def currentBehavior: Behavior[T] = current
  def isAlive: Boolean = Behavior.isAlive(current)

  private def handleException: Catcher[Unit] = {
    case NonFatal(e) ⇒
      try Behavior.canonicalize(Behavior.interpretSignal(current, ctx, PostStop), current, ctx) // TODO why canonicalize here?
      catch {
        case NonFatal(_) ⇒ /* ignore, real is logging */
      }
      throw e
  }

  /**
    * Send the msg to the behavior and record any [[Effect]]s
    */
  def run(msg: T): Unit = {
    try {
      current = Behavior.canonicalize(Behavior.interpretMessage(current, ctx, msg), current, ctx)
    } catch handleException
  }

  /**
    * Send the msg to the behavior and record any [[Effect]]s
    * Doesn't canonicalize the behavior
    */
  def runUncanonical(msg: T): Unit = {
    try {
      current = Behavior.interpretMessage(current, ctx, msg)
    } catch handleException
  }


  /**
    * Send the signal to the beheavior and record any [[Effect]]s
    */
  def signal(signal: Signal): Unit = {
    try {
      current = Behavior.canonicalize(Behavior.interpretSignal(current, ctx, signal), current, ctx)
    } catch handleException
  }

}
