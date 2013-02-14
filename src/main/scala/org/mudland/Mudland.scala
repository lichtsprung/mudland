package org.mudland

import scala.concurrent.duration._
import akka.actor.{ActorRef, ActorLogging, Actor, Props}
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
import org.mudland.GameServer.{Register, HandlePlayerAction, TextMessage}
import java.nio.ByteBuffer

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

class PlayerHandler extends Actor with ActorLogging {
  val ioBridge = IOExtension(context.system).ioBridge()
  var handle: Option[Connection] = None
  var registry = context.actorFor("/user/registry")

  def receive = {
    case HandlePlayerAction(buffer) =>
      val tokens = buffer.array.asString.trim.split(" ").toList
      val verb = tokens.head
      verb match {
        case "STOP" =>
          ioBridge ! IOBridge.Send(handle.get, BufferBuilder("Shutting Down...").toByteBuffer)
          log.info("Shutting down MUDland...")
          context.system.shutdown()
        case "login" =>
          log.info("Changing name to {}", tokens.tail.head)
        case x =>
          log.info("Received {} from {}, echoing!", x, handle)
          sendMessage(x)
      }
    case Register(connection) => handle = Option[Connection](connection)
  }

  def sendMessage(text: String) = ioBridge ! IOBridge.Send(handle.get, BufferBuilder(text).toByteBuffer)
}


class Player(var name: String, var connection: Connection) {
}

object Player {
  def apply(name: String, connection: Connection) = new Player(name, connection)
}


