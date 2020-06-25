package indigo.shared.subsystems

import indigo.shared.Outcome
import indigo.shared.events.GlobalEvent
import indigo.shared.scenegraph.SceneUpdateFragment
import indigo.shared.FrameContext
import indigo.shared.Lens

trait SubSystem
object SubSystem {
  trait Stateful extends SubSystem {
    type EventType

    val eventFilter: GlobalEvent => Option[EventType]

    def update(context: FrameContext): EventType => Outcome[SubSystem]

    def render(context: FrameContext): SceneUpdateFragment
  }

  trait Stateless[GameModel] extends SubSystem {
    type EventType
    type SubSystemModel

    val sceneModelLens: Lens[GameModel, SubSystemModel]

    val eventFilter: GlobalEvent => Option[EventType]

    def update(context: FrameContext, model: SubSystemModel): EventType => Outcome[GameModel]

    def render(context: FrameContext, model: SubSystemModel): SceneUpdateFragment
  }
}

/*
trait Scene[GameModel, ViewModel] {
  type SceneModel
  type SceneViewModel

  val name: SceneName
  val sceneModelLens: Lens[GameModel, SceneModel]
  val sceneViewModelLens: Lens[ViewModel, SceneViewModel]

  val sceneSubSystems: Set[SubSystem]

  def updateSceneModel(context: FrameContext, sceneModel: SceneModel): GlobalEvent => Outcome[SceneModel]
  def updateSceneViewModel(context: FrameContext, sceneModel: SceneModel, sceneViewModel: SceneViewModel): Outcome[SceneViewModel]
  def updateSceneView(context: FrameContext, sceneModel: SceneModel, sceneViewModel: SceneViewModel): SceneUpdateFragment

}
 */
