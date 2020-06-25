package indigo.shared.subsystems

import indigo.shared.Outcome
import indigo.shared.events.GlobalEvent
import indigo.shared.scenegraph.SceneUpdateFragment
import indigo.shared.FrameContext
import indigo.shared.datatypes.BindingKey

sealed trait SubSystem
@SuppressWarnings(Array("org.wartremover.warts.LeakingSealed"))
object SubSystem {
  trait Stateful extends SubSystem {
    type EventType

    def eventFilter: GlobalEvent => Option[EventType]

    def update(context: FrameContext): EventType => Outcome[SubSystem.Stateful]

    def render(context: FrameContext): SceneUpdateFragment
  }

  trait Stateless extends SubSystem {
    type EventType
    type SubSystemModel

    def key: BindingKey

    def eventFilter: GlobalEvent => Option[EventType]

    def initialModel: SubSystemModel

    def update(context: FrameContext, model: SubSystemModel): EventType => Outcome[SubSystemModel]

    def render(context: FrameContext, model: SubSystemModel): SceneUpdateFragment
  }
}
