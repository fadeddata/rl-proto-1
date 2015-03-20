package actors

import java.util.UUID
import actors.Client._
import actors.WorldManager._
import akka.actor._

object World {
  case class AddClient(client: ActorRef)
  case class GreetClient(map: Seq[Seq[String]])
}

class World(id: UUID, name: String) extends Actor with ActorLogging {
  import World._
  var clientId = 1
  var clients = Set.empty[(Int, ActorRef)]
  var map = Seq.tabulate(80, 25) { (col, row) =>
    if (row == 0 | col == 0 | row == 24 | col == 79) "#" else "."
  }

  def receive = {
    case AddClient(client) =>
      log.debug(s"World($id/$name).AddClient($client)")
      val pair = (clientId, client)
      clients = clients + pair
      clientId = clientId + 1
      client ! GreetClient(map)
      clients.foreach { oldClient =>
        if (oldClient._2 != client) {
          oldClient._2 ! AddEntity(clientId)
          client ! AddEntity(oldClient._1)
        }
      }

    case RemoveClient(client) =>
      clients.find(_._2 == client).foreach { pair =>
        val id  = pair._1
        clients = clients - pair
        clients.foreach { client =>
          client._2 ! RemoveEntity(id)
        }
      }

    case GetMap => sender ! map

    case GetName(_) =>
      log.debug(s"World($id/$name).GetName")
      sender ! name

    case MovePlayer(newX, newY, oldX, oldY) =>
      val idOfMoved = clients.filter(_._2 == sender).head._1
      clients.foreach { case (id, client) =>
          if (id != idOfMoved) {
            client ! MoveEntity(idOfMoved, newX, newY)
          }
      }

  }
}
