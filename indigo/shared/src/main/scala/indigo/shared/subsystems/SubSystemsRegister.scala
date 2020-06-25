package indigo.shared.subsystems

import indigo.shared.Outcome
import indigo.shared.Outcome._
import indigo.shared.events.GlobalEvent
import indigo.shared.scenegraph.SceneUpdateFragment
import scala.collection.mutable.ListBuffer
import indigo.shared.FrameContext
import scala.collection.mutable

@SuppressWarnings(Array("org.wartremover.warts.NonUnitStatements"))
final class SubSystemsRegister(subSystems: List[SubSystem]) {

  @SuppressWarnings(Array("org.wartremover.warts.MutableDataStructures"))
  val statefulSubSystems: ListBuffer[SubSystem.Stateful] =
    ListBuffer.from(subSystems.collect { case s: SubSystem.Stateful => s })
  val stateLessSubSystems: List[SubSystem.Stateless] =
    subSystems.collect { case s: SubSystem.Stateless => s }

  @SuppressWarnings(Array("org.wartremover.warts.MutableDataStructures"))
  val stateMap: mutable.HashMap[String, Object] = new mutable.HashMap[String, Object]()

  @SuppressWarnings(Array("org.wartremover.warts.AsInstanceOf"))
  def update(frameContext: FrameContext): GlobalEvent => Outcome[SubSystemsRegister] =
    (e: GlobalEvent) => {
      val stateful: Outcome[List[SubSystem.Stateful]] =
        statefulSubSystems.toList.map { ss =>
          ss.eventFilter(e) match {
            case None =>
              Outcome(ss)

            case Some(ee) =>
              ss.update(frameContext)(ee)
          }
        }.sequence

      val statelessEvents: List[GlobalEvent] =
        stateLessSubSystems.flatMap { ss =>
          ss.eventFilter(e) match {
            case None =>
              Nil

            case Some(ee) =>
              val key                             = ss.key.value
              val model: ss.SubSystemModel        = stateMap.get(key).map(_.asInstanceOf[ss.SubSystemModel]).getOrElse(ss.initialModel)
              val out: Outcome[ss.SubSystemModel] = ss.update(frameContext, model.asInstanceOf[ss.SubSystemModel])(ee)
              stateMap.put(key, out.state.asInstanceOf[Object])
              out.globalEvents
          }
        }

      statefulSubSystems.clear()
      statefulSubSystems ++= stateful.state

      Outcome(this).addGlobalEvents(stateful.globalEvents ++ statelessEvents)
    }

  @SuppressWarnings(Array("org.wartremover.warts.AsInstanceOf"))
  def render(frameContext: FrameContext): SceneUpdateFragment = {
    val stateful: SceneUpdateFragment =
      statefulSubSystems
        .map(_.render(frameContext))
        .foldLeft(SceneUpdateFragment.empty)(_ |+| _)

    val stateless: SceneUpdateFragment =
      stateLessSubSystems
        .map { ss =>
          ss.render(
            frameContext,
            stateMap(ss.key.value).asInstanceOf[ss.SubSystemModel]
          )
        }
        .foldLeft(SceneUpdateFragment.empty)(_ |+| _)

    stateful |+| stateless
  }

  def size: Int =
    statefulSubSystems.size

}
