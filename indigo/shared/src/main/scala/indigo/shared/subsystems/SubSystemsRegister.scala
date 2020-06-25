package indigo.shared.subsystems

import indigo.shared.Outcome
import indigo.shared.Outcome._
import indigo.shared.events.GlobalEvent
import indigo.shared.scenegraph.SceneUpdateFragment
import scala.collection.mutable.ListBuffer
import indigo.shared.FrameContext

@SuppressWarnings(Array("org.wartremover.warts.NonUnitStatements"))
final class SubSystemsRegister[GameModel](subSystems: List[SubSystem]) {

  @SuppressWarnings(Array("org.wartremover.warts.MutableDataStructures"))
  val registeredSubSystems: ListBuffer[SubSystem] = ListBuffer.from(subSystems)

  @SuppressWarnings(Array("org.wartremover.warts.StringPlusAny"))
  def update(frameContext: FrameContext, model: GameModel): GlobalEvent => Outcome[SubSystemsRegister[GameModel]] =
    (e: GlobalEvent) => {
      registeredSubSystems.toList
        .map {
          case ss: SubSystem.Stateful =>
            ss.eventFilter(e)
              .map(ee => ss.update(frameContext)(ee))
              .getOrElse(Outcome(ss, Nil))

          case ss: SubSystem.Stateless[GameModel] =>
            ss.eventFilter(e)
              .map { ee => 
                val ssm = ss.sceneModelLens.get(model)
                val updateModel = ss.update(frameContext, ssm)(ee)
                
              }
              .getOrElse(Outcome(ss, Nil))

            Outcome(ss)

          case s =>
            Outcome(s)
        }
        .sequence
        .mapState { l =>
          registeredSubSystems.clear()
          registeredSubSystems ++= l
          this
        }
    }

  def render(frameContext: FrameContext): SceneUpdateFragment =
    registeredSubSystems
      .map {
        case ss: SubSystem.Stateful =>
          ss.render(frameContext)

        case _: SubSystem.Stateless[GameModel] =>
          SceneUpdateFragment.empty

        case _ =>
          SceneUpdateFragment.empty
      }
      .foldLeft(SceneUpdateFragment.empty)(_ |+| _)

  def size: Int =
    registeredSubSystems.size

}
