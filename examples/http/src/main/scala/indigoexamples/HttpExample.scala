package indigoexamples

import indigo._
import indigoextras.ui._

import scala.scalajs.js.annotation._

@JSExportTopLevel("IndigoGame")
object HttpExample extends IndigoDemo[Unit, Unit, Unit, Button] {

  def parseFlags(flags: Map[String,String]): Unit = ()

  def config(flagData: Unit): GameConfig = defaultGameConfig

  def assets(flagData: Unit): Set[AssetType] = Set(AssetType.Image(AssetName("graphics"), AssetPath("assets/graphics.png")))

  val fonts: Set[FontInfo] = Set()

  val animations: Set[Animation] = Set()

  val subSystems: Set[SubSystem] = Set()

  def setup(flagData: Unit, gameConfig: GameConfig, assetCollection: AssetCollection, dice: Dice): Startup[StartupErrors, Unit] =
    Startup.Success(())

  def initialModel(startupData: Unit): Unit =
    ()

  def initialViewModel(startupData: Unit, model: Unit): Button =
    Button(
      ButtonAssets(
        up = Graphic(0, 0, 16, 16, 2, Material.Textured(AssetName("graphics"))).withCrop(32, 0, 16, 16),
        over = Graphic(0, 0, 16, 16, 2, Material.Textured(AssetName("graphics"))).withCrop(32, 16, 16, 16),
        down = Graphic(0, 0, 16, 16, 2, Material.Textured(AssetName("graphics"))).withCrop(32, 32, 16, 16)
      ),
      bounds = Rectangle(10, 10, 16, 16),
      depth = Depth(2)
    ).withUpAction {
      List(HttpRequest.GET("http://localhost:8080/ping"))
    }

  def updateModel(context: FrameContext, model: Unit): GlobalEvent => Outcome[Unit] = {
    case HttpResponse(status, headers, body) =>
      println("Status code: " + status.toString)
      println("Headers: " + headers.map(p => p._1 + ": " + p._2).mkString(", "))
      println("Body: " + body.getOrElse("<EMPTY>"))
      Outcome(model)

    case HttpError =>
      println("Http error message")
      Outcome(model)

    case _ =>
      Outcome(model)
  }

  def updateViewModel(context: FrameContext, model: Unit, viewModel: Button): Outcome[Button] =
    viewModel.update(context.inputState.mouse)

  def present(context: FrameContext, model: Unit, viewModel: Button): SceneUpdateFragment =
    SceneUpdateFragment(viewModel.draw)
}
