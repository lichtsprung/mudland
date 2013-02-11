package org.mudland

import scala.concurrent.duration._
import akka.actor.{ActorLogging, Actor, Props}
import akka.pattern.ask
import spray.util._
import spray.io._
import java.net.InetSocketAddress
import spray.io.IOBridge.Key
import org.mudland.Implicits._
import collection.mutable
import org.mudland.PlayerRegistry._
import org.mudland.PlayerRegistry.PlayerConnectionRequest
import org.mudland.PlayerRegistry.RegisterPlayer
import org.mudland.GameServer.TextMessage

/**
 * Main Object that starts the game server.
 */
object Mudland extends App {
  val gameServer = system.actorOf(Props[GameServer], "game-server")

  gameServer
    .ask(IOServer.Bind("localhost", 8888))(1 second span)
    .onSuccess {
    case IOServer.Bound(endpoint, _) =>
      println("MUDland server started on port 8888")
  }
}

import scala.concurrent.ExecutionContext.Implicits.global

class GameServer extends IOServer {
  val ioBridge = IOExtension(context.system).ioBridge()
  val registry = context.actorOf(Props[PlayerRegistry], "registry")

  override def bound(endpoint: InetSocketAddress, key: Key, tag: Any): Receive = {
    case IOBridge.Received(handle, buffer) =>
      // TODO should be forwarded to connection handler actor
      val tokens = buffer.array.asString.trim.split(" ").toList
      val verb = tokens.head
      verb match {
        case "STOP" =>
          ioBridge ! IOBridge.Send(handle, BufferBuilder("Shutting Down...").toByteBuffer)
          log.info("Shutting down MUDland...")
          context.system.shutdown()
        case "login" =>
          log.info("Changing name to {}", tokens.tail.head)
          registry ! PlayerNameChangeRequest(handle, tokens.tail.head)
        case x =>
          log.info("Received {} from {}, echoing!", x, handle)
          var playerName = "newPlayer"
          registry.ask(PlayerNameRequest(handle))(1 second span)
            .onSuccess {
            case PlayerNameResponse(name) => playerName = name
          }
          self ! TextMessage(x, playerName)
      }
    case IOBridge.Connected(key, tag) =>
      // TODO Should spawn a new server actor per connection
      val handle = createConnection(key, tag)
      sender ! IOBridge.Register(handle)
      ioBridge ! IOBridge.Send(handle, BufferBuilder("Welcome! \nLogin or Register a character? ").toByteBuffer)
      val newPlayer = Player("newPlayer", handle)
      registry ! RegisterPlayer(newPlayer)
    case TextMessage(text, name) =>
      registry.ask(PlayerConnectionRequest(name))(1 second span)
        .onSuccess {
        case PlayerConnectionResponse(connection) => ioBridge ! IOBridge.Send(connection, BufferBuilder(text).toByteBuffer)
      }


    case IOBridge.Closed(_, reason) =>
      log.info("Connection closed. Reason: {}", reason)

  }

}

object GameServer {

  sealed trait GameServerCommand

  case class TextMessage(text: String, name: String) extends GameServerCommand

}

class PlayerRegistry extends Actor with ActorLogging {
  private var registry = mutable.HashMap[Connection, Player]()

  def registerPlayer(player: Player) = {
    registry += player.connection -> player
  }

  def player(name: String) = registry.values.filter(p => p.name == name).head

  def receive = {
    case RegisterPlayer(player) => registerPlayer(player)
    case PlayerConnectionRequest(name: String) => sender ! PlayerConnectionResponse(player(name).connection)
    case PlayerNameRequest(connection) => sender ! PlayerNameResponse(registry.get(connection).get.name)
    case PlayerNameChangeRequest(connection, name) =>
      val player = registry.get(connection).get
      log.info("Name change from {} to {}", player.name, name)
      player.name = name
  }
}

object PlayerRegistry {

  case class RegisterPlayer(player: Player)

  case class PlayerNameRequest(connection: Connection)

  case class PlayerNameChangeRequest(connection: Connection, name: String)

  case class PlayerNameResponse(name: String)

  case class PlayerConnectionRequest(name: String)

  case class PlayerConnectionResponse(connection: Connection)

}


class Player(var name: String, var connection: Connection) {
}

object Player {
  def apply(name: String, connection: Connection) = new Player(name, connection)
}


