package actors

import java.util.UUID
import actors.World._
import actors.WorldManager._
import akka.actor._
import akka.util.Timeout
import play.api.libs.json._
import julienrf.variants.Variants
import akka.pattern.ask
import scala.concurrent.duration._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scala.util.{Success, Try, Failure}

object Client {
  sealed trait ClientMessage
  case object GetMap extends ClientMessage
  case class UpdateMap(map: Seq[Seq[String]]) extends ClientMessage
  case class MovePlayer(newX: Int, newY: Int, oldX: Int, oldY: Int) extends ClientMessage
  case class AddEntity(id: UUID) extends ClientMessage
  case class MoveEntity(id: UUID, newX: Int, newY: Int) extends ClientMessage
  case class RemoveEntity(id: UUID) extends ClientMessage

  case class RemoveClient(client: ActorRef)

  implicit val format: Format[ClientMessage] = Variants.format[ClientMessage]("$message")

  def props(out: ActorRef, worldManager: ActorRef, worldId: UUID) = Props(new Client(out, worldManager, worldId))
}

class Client(out: ActorRef, worldManager: ActorRef, worldId: UUID) extends Actor with ActorLogging {
  import Client._
  var world: ActorRef = _
  var map: Seq[Seq[String]] = _
  implicit val timeout = Timeout(5 seconds)
  var playerX = 1
  var playerY = 1
  worldManager ! JoinWorld(worldId)

  def receive = {
    case GreetClient(worldMap) =>
      log.debug("Client.GreetClient")
      world = sender
      map = worldMap

    case GetMap =>
      log.debug("Client.GetMap")
      (world ? GetMap).mapTo[Seq[Seq[String]]].foreach(map => send(UpdateMap(map)))

    case MovePlayer(newX, newY, oldX, oldY) =>
      log.debug(s"Client.MovePlayer($newX, $newY, $oldX, $oldY)")
      Try {
        val tile = map(newX)(newY)
        if (tile == "#") {
          send(MovePlayer(playerX, playerY, newX, newY))
        } else {
          playerX = newX
          playerY = newY
          world ! MovePlayer(newX, newY, oldX, oldY)
        }
      } match {
        case Failure(_) => send(MovePlayer(playerX, playerY, newX, newY))
        case _ =>
      }

    case message: AddEntity => send(message)
    case message: MoveEntity => send(message)
    case message: RemoveEntity => send(message)

    case json:JsObject =>
      log.debug("Client.JsObject")
      json.validate[ClientMessage] match {
        case JsSuccess(message, path) => self ! message
        case JsError(errors) => log.debug(s"unrecognized json: $json errors: " + errors)
      }

    case msg => log.debug(msg.toString)
  }

  def send(message: ClientMessage): Unit = {
    out ! Json.toJson(message)
  }

  override def postStop() = {
    world ! RemoveClient(self)
  }
}
