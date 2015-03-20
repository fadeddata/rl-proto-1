package actors

import java.util.UUID

import actors.World._
import akka.actor._
import akka.util.Timeout
import scala.collection.immutable._
import play.api.libs.concurrent.Akka
import play.api.Play.current
import akka.pattern.ask
import scala.concurrent.duration._
import play.api.libs.concurrent.Execution.Implicits.defaultContext

object WorldManager {
  case class CreateWorld(name: String)
  case class GetName(id: UUID)
  case class JoinWorld(worldId: UUID)
  case object ListWorlds

  val service = Akka.system.actorOf(Props[WorldManager], "WorldManager")
}

class WorldManager extends Actor with ActorLogging {
  import actors.WorldManager._

  implicit val timeout = Timeout(5 seconds)
  var worldUsers = Map.empty[UUID, Set[ActorRef]]
  var worlds = Map.empty[UUID, ActorRef]

  def receive = {
    case CreateWorld(name) =>
      log.debug(s"WorldManager.CreateWorld($name)")
      val id = UUID.randomUUID()
      val world = context.actorOf(Props(classOf[World], id, name), "World-" + id)
      worlds = worlds + (id -> world)

    case JoinWorld(worldId) =>
      log.debug(s"WorldManager.JoinWorld($worldId)")
      worlds.get(worldId).foreach(world => world ! AddClient(sender))

    case ListWorlds =>
      log.debug("WorldManager.ListWorlds")
      val ret = worlds.map { case (id, world) =>
        (world ? GetName(id)).mapTo[String].map(name => (id, name))
      }

      sender ! ret

    case msg @ GetName(id) =>
      log.debug(s"WorldManager.GetName($id)")
      worlds.get(id).foreach(world => world forward msg)
  }
}