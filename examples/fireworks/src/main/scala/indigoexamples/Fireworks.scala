package indigoexamples

import indigo._
import indigoextras.subsystems.FPSCounter

import indigoexamples.automata.LaunchPadAutomata
import indigoexamples.automata.RocketAutomata
import indigoexamples.automata.TrailAutomata
import indigoexamples.automata.FlareAutomata
import indigoexamples.model.{Projectiles, LaunchPad}
import indigoextras.geometry.Vertex
import indigoextras.subsystems.AutomataEvent

import scala.scalajs.js.annotation._

@JSExportTopLevel("IndigoGame")
object Fireworks extends IndigoDemo[Unit, FireworksStartupData, FireworksModel, Unit] {

  def parseFlags(flags: Map[String, String]): Unit = ()

  val targetFPS: Int = 60

  val magnification: Int = 3

  def config(flagData: Unit): GameConfig =
    defaultGameConfig
      .withFrameRate(targetFPS)
      .withMagnification(magnification)
      .withViewport(GameViewport.at720p)

  def assets(flagData: Unit): Set[AssetType] =
    Assets.assets

  val fonts: Set[FontInfo] =
    Set(FontDetails.fontInfo)

  val animations: Set[Animation] =
    Set()

  def toScreenSpace(viewportDimensions: Rectangle): Vertex => Point =
    Projectiles.toScreenSpace(viewportDimensions)

  def launchFireworks(dice: Dice, toScreenSpace: Vertex => Point): List[AutomataEvent.Spawn] =
    List.fill(dice.roll(5) + 5)(
      LaunchPadAutomata.spawnEvent(
        LaunchPad.generateLaunchPad(dice),
        toScreenSpace
      )
    )

  val subSystems: Set[SubSystem] =
    Set(
      FPSCounter.subSystem(FontDetails.fontKey, Point(5, 5), targetFPS),
      LaunchPadAutomata.automata,
      RocketAutomata.automata,
      TrailAutomata.automata,
      FlareAutomata.automata
    )

  def setup(flagData: Unit, gameConfig: GameConfig, assetCollection: AssetCollection, dice: Dice): Startup[StartupErrors, FireworksStartupData] =
    Startup.Success(
      FireworksStartupData(
        toScreenSpace(gameConfig.viewport.giveDimensions(magnification))
      )
    )

  def initialModel(startupData: FireworksStartupData): FireworksModel =
    FireworksModel(startupData.toScreenSpace)

  def initialViewModel(startupData: FireworksStartupData, model: FireworksModel): Unit =
    ()

  def updateModel(context: FrameContext, model: FireworksModel): GlobalEvent => Outcome[FireworksModel] = {
    case KeyboardEvent.KeyUp(Keys.SPACE) =>
      Outcome(model, launchFireworks(context.dice, model.toScreenSpace))

    case _ =>
      Outcome(model)
  }

  def updateViewModel(context: FrameContext, model: FireworksModel, viewModel: Unit): Outcome[Unit] =
    Outcome(())

  def present(context: FrameContext, model: FireworksModel, viewModel: Unit): SceneUpdateFragment =
    SceneUpdateFragment.empty

}

final case class FireworksStartupData(toScreenSpace: Vertex => Point)
final case class FireworksModel(toScreenSpace: Vertex => Point)
