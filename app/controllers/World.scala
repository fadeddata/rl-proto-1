package controllers

import java.util.UUID

import akka.util.Timeout
import play.api.data._
import play.api.data.Forms._
import play.api.libs.json._
import play.api.mvc._
import actors._, WorldManager._
import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import akka.pattern.ask
import scala.concurrent.Future
import scala.concurrent.duration._

object World extends Controller {
  implicit val timeout = Timeout(5 seconds)

  def newWorldForm: Form[CreateWorld] = Form(
    mapping(
      "name" -> nonEmptyText
    )(CreateWorld.apply)(CreateWorld.unapply)
  )

  def index = Action.async {
    val worlds = (WorldManager.service ? ListWorlds).mapTo[Iterable[Future[(UUID, String)]]].flatMap(i => Future.sequence(i))
    worlds.map(list => Ok(views.html.world.index(list.toList)))
  }

  def form = Action {
    Ok(views.html.world.form(newWorldForm))
  }

  def show(worldId: String) = Action.async {
    val id = UUID.fromString(worldId)
    (WorldManager.service ? GetName(id)).mapTo[String].map { name =>
      Ok(views.html.world.show(id, name))
    }
  }

  def post = Action { implicit request =>
    newWorldForm.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(views.html.world.form(formWithErrors))
      },
      createWorld => {
        WorldManager.service ! createWorld
        Redirect(routes.World.index)
      }
    )
  }

  def socket(worldId: String) = WebSocket.acceptWithActor[JsValue, JsValue] { request => out =>
    Client.props(out, WorldManager.service, UUID.fromString(worldId))
  }
}