package indigoextras.subsystems

import indigo.shared.subsystems.SubSystem
import indigo.shared.datatypes.Point
import indigo.shared.datatypes.FontKey
import indigo.shared.time.Seconds
import indigo.shared.events.GlobalEvent
import indigo.shared.FrameContext
import indigo.shared.Outcome
import indigo.shared.scenegraph.SceneUpdateFragment
import indigo.shared.datatypes.RGBA
import indigo.shared.AsString
import indigo.shared.scenegraph.Text
import indigo.shared.events.FrameTick
import indigo.shared.datatypes.BindingKey

@SuppressWarnings(Array("org.wartremover.warts.Var"))
final class FPSCounter(fontKey: FontKey, position: Point, targetFPS: Int) extends SubSystem.Stateless {

  type EventType      = GlobalEvent
  type SubSystemModel = FPSCounterState

  val key: BindingKey =
    BindingKey("fps counter") // There is only one FPS, recording the state twice makes no sense.

  val eventFilter: GlobalEvent => Option[GlobalEvent] = {
    case FrameTick => Option(FrameTick)
    case _         => None
  }

  def initialModel: FPSCounterState =
    FPSCounterState.default

  def update(frameContext: FrameContext, model: FPSCounterState): GlobalEvent => Outcome[FPSCounterState] = {
    case FrameTick =>
      if (frameContext.gameTime.running >= (model.lastInterval + Seconds(1)))
        Outcome(
          FPSCounterState(
            fps = Math.min(targetFPS, model.frameCountSinceInterval + 1),
            lastInterval = frameContext.gameTime.running,
            frameCountSinceInterval = 0
          )
        )
      else
        Outcome(model.copy(frameCountSinceInterval = model.frameCountSinceInterval + 1))
  }

  def render(frameContext: FrameContext, model: FPSCounterState): SceneUpdateFragment =
    SceneUpdateFragment.empty
      .addUiLayerNodes(Text(fpsCount(model.fps), position.x, position.y, 1, fontKey).withTint(pickTint(model.fps)))

  def fpsCount(fps: Int)(implicit showI: AsString[Int]): String =
    s"""FPS: ${showI.show(fps)}"""

  def pickTint(fps: Int): RGBA =
    if (fps > targetFPS - (targetFPS * 0.05)) RGBA.Green
    else if (fps > targetFPS / 2) RGBA.Yellow
    else RGBA.Red
}

object FPSCounter {

  def apply(fontKey: FontKey, position: Point, targetFPS: Int): FPSCounter =
    new FPSCounter(fontKey, position, targetFPS)

  def subSystem(fontKey: FontKey, position: Point, targetFPS: Int): FPSCounter =
    FPSCounter(fontKey, position, targetFPS)

}

final case class FPSCounterState(fps: Int, lastInterval: Seconds, frameCountSinceInterval: Int)
object FPSCounterState {
  def default: FPSCounterState =
    FPSCounterState(0, Seconds.zero, 0)
}
