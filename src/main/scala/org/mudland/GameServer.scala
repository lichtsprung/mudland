package org.mudland

import spray.io._
import spray.util._
import akka.actor.{ActorRef, Props}
import java.net.InetSocketAddress
import spray.io.IOBridge.{Key, Connection}
import collection.mutable
import java.nio.ByteBuffer
import org.mudland.GameServer.{BroadcastMessage, HandlePlayerAction}

/**
 * Game server that handles the network requests and forwards them to the actor that is handling the player instance.
 */
class GameServer extends IOServer {
  val ioBridge = IOExtension(context.system).ioBridge()
  var connections = mutable.Map[Connection, ActorRef]()

  override def bound(endpoint: InetSocketAddress, key: Key, tag: Any): Receive = {
    case IOBridge.Received(handle, buffer) =>
      val tokens = buffer.array.asString.trim.split(" ").toList
      val verb = tokens.head
      verb match {
        case "login" if tokens.tail.size > 0 =>
          log.info("Logging in {}", tokens.tail.head)
          val playerHandler = context.actorOf(Props[PlayerHandler])
          playerHandler ! GameServer.Register(handle, tokens.tail.head)
          connections += handle -> playerHandler
          sendMessage("Welcome,  " + tokens.tail.head + "\n", handle)
        case _ => connections.get(handle).foreach(c => c ! HandlePlayerAction(buffer))
      }
    case BroadcastMessage(text, name) => connections.keys.foreach(connection => sendMessage(name + " says: " + text, connection))
    case IOBridge.Connected(key, tag) =>
      val handle = createConnection(key, tag)
      sender ! IOBridge.Register(handle)
      sendMessage("Hello, stranger! Who are you? (Use: login <name>)\n", handle)
    case IOBridge.Closed(_, reason) =>
      log.info("Connection closed. Reason: {}", reason)
  }

  def sendMessage(text: String, handle: Connection) = ioBridge ! IOBridge.Send(handle, BufferBuilder(text + "\n").toByteBuffer)
}


object GameServer {

  sealed trait GameServerCommand

  case class HandlePlayerAction(buffer: ByteBuffer)

  case class Register(connection: Connection, username: String)

  case class TextMessage(text: String, name: String) extends GameServerCommand

  case class BroadcastMessage(text: String, name: String) extends GameServerCommand

}