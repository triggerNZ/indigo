package indigo.entry

import utest._
import indigo.shared.assets.AssetType.Text
import indigo.shared.subsystems.SubSystem
import indigo.shared.events.GlobalEvent
import indigo.shared.Outcome
import indigo.shared.dice.Dice
import indigo.shared.time.GameTime
import indigo.shared.scenegraph.SceneUpdateFragment
import indigo.shared.subsystems.SubSystemsRegister
import indigo.shared.events.InputState
import indigo.shared.BoundaryLocator
import indigo.shared.AnimationsRegister
import indigo.shared.FontRegister
import indigo.shared.FrameContext
import indigo.shared.datatypes.BindingKey

object GameWithSubSystemsTests extends TestSuite {

  import GameTestFixtures._

  val tests: Tests =
    Tests {

      "should be able to run model update across a game and it's subsytems and produce the right outcome" - {
        val updated = GameWithSubSystems.update(modelUpdate)(context, gameWithSubSystems)

        val outcome = updated(EventsOnlyEvent.Increment)

        outcome.state.model.text ==> ""
        outcome.state.subSystemsRegister.size ==> 1
        outcome.globalEvents.length ==> 3
        outcome.globalEvents.contains(EventsOnlyEvent.OnePerCount) ==> true
        outcome.globalEvents.contains(EventsOnlyEvent.Total(1)) ==> true
        outcome.globalEvents.contains(EventsOnlyEvent.Decrement) ==> true
      }

      "should be able to update the view model" - {
        val outcome = GameWithSubSystems.updateViewModel(viewModelUpdate)(context, gameWithSubSystems, 0)
        outcome.state ==> 10
        outcome.globalEvents.length ==> 1
        outcome.globalEvents.contains(EventsOnlyEvent.Increment) ==> true
      }

    }

}

object GameTestFixtures {

  def context: FrameContext =
    new FrameContext(
      GameTime.zero,
      Dice.loaded(0),
      InputState.default,
      new BoundaryLocator(new AnimationsRegister, new FontRegister)
    )

  val subSystem =
    new EventsOnlySubSystem()

  val register =
    new SubSystemsRegister(List(subSystem))

  val model =
    GameModel("")

  val gameWithSubSystems =
    new GameWithSubSystems[GameModel](model, register)

  val modelUpdate: (FrameContext, GameModel) => GlobalEvent => Outcome[GameModel] =
    (_, m) => _ => Outcome(m).addGlobalEvents(EventsOnlyEvent.Decrement)

  val viewModelUpdate: (FrameContext, GameModel, Int) => Outcome[Int] =
    (_, _, viewModel) => Outcome(viewModel + 10).addGlobalEvents(EventsOnlyEvent.Increment)

  final case class GameModel(text: String)

  final class EventsOnlySubSystem extends SubSystem.Stateless {

    type EventType      = EventsOnlyEvent
    type SubSystemModel = Int

    val key: BindingKey = BindingKey("Events only SubSystem")

    def initialModel: Int = 0

    val eventFilter: GlobalEvent => Option[EventsOnlyEvent] = {
      case e: EventsOnlyEvent => Some(e)
      case _                  => None
    }

    def update(context: FrameContext, count: Int): EventsOnlyEvent => Outcome[Int] = {
      case EventsOnlyEvent.Increment =>
        val newCount = count + 1

        Outcome(newCount)
          .addGlobalEvents(EventsOnlyEvent.Total(newCount))
          .addGlobalEvents(List.fill(newCount)(EventsOnlyEvent.OnePerCount))

      case EventsOnlyEvent.Decrement =>
        Outcome(count - 1)

      case _ =>
        Outcome(count)
    }

    def render(context: FrameContext, count: Int): SceneUpdateFragment =
      SceneUpdateFragment.empty

  }

  sealed trait EventsOnlyEvent extends GlobalEvent
  object EventsOnlyEvent {
    case object Increment              extends EventsOnlyEvent
    case object Decrement              extends EventsOnlyEvent
    final case class Total(count: Int) extends EventsOnlyEvent
    case object OnePerCount             extends EventsOnlyEvent
  }
}
