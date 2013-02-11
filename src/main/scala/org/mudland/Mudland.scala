package org.mudland

import scala.concurrent.duration._
import akka.actor.{Props}
import akka.pattern.ask
import spray.util._
import spray.io._
import java.net.InetSocketAddress
import spray.io.IOBridge.Key
import org.mudland.Implicits._
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


class GameServer extends IOServer {
  val ioBridge = IOExtension(context.system).ioBridge()

  override def bound(endpoint: InetSocketAddress, bindingKey: Key, bindingTag: Any): Receive = {
    super.bound(endpoint, bindingKey, bindingTag) orElse {
      case IOBridge.Received(handle, buffer) =>
        val tokens = buffer.array.asString.trim.split(" ").toList
        val verb = tokens.head
        verb match {
          case "STOP" =>
            ioBridge ! IOBridge.Send(handle, BufferBuilder("Shutting Down...").toByteBuffer)
            log.info("Shutting down MUDland...")
            context.system.shutdown()
          case x =>
            log.info("Received {} from {}, echoing!", x, handle)
            ioBridge ! IOBridge.Send(handle, buffer, None)
        }


      case IOBridge.Closed(_, reason) =>
        log.info("Connection closed. Reason: {}", reason)

    }
  }

}

object GameServer{
  sealed trait GameServerCommand
  case class TextMessage(text: String) extends GameServerCommand
}



