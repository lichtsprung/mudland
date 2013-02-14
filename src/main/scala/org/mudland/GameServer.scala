package org.mudland

import spray.io._
import akka.actor.{ActorRef, Props}
import java.net.InetSocketAddress
import spray.io.IOBridge.{Key, Connection}
import collection.mutable
import java.nio.ByteBuffer
import org.mudland.GameServer.HandlePlayerAction

/**
 * Game server that handles the network requests and forwards them to the actor that is handling the player instance.
 */
class GameServer extends IOServer {
  val ioBridge = IOExtension(context.system).ioBridge()
  val registry = context.actorOf(Props[PlayerRegistry], "registry")
  var connections = mutable.HashMap[Connection, ActorRef]()

  override def bound(endpoint: InetSocketAddress, key: Key, tag: Any): Receive = {
    case IOBridge.Received(handle, buffer) =>
      connections.get(handle).get ! HandlePlayerAction(buffer)
    case IOBridge.Connected(key, tag) =>
      val handle = createConnection(key, tag)
      sender ! IOBridge.Register(handle)
      val playerHandler = context.actorOf(Props[PlayerHandler])
      playerHandler ! GameServer.Register(handle)
      connections += handle -> playerHandler
    case IOBridge.Closed(_, reason) =>
      log.info("Connection closed. Reason: {}", reason)

  }
}


object GameServer {

  sealed trait GameServerCommand

  case class HandlePlayerAction(buffer: ByteBuffer)

  case class Register(connection: Connection)

  case class TextMessage(text: String, name: String) extends GameServerCommand

}