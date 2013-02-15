package org.mudland

import scala.concurrent.duration._
import akka.actor.{ActorLogging, Actor, Props}
import akka.pattern.ask
import spray.util._
import spray.io._
import org.mudland.Implicits._
import org.mudland.GameServer.{BroadcastMessage, Register, HandlePlayerAction}
import collection.mutable

/**
 * Main Object that starts the game server.
 */
object Mudland extends App {
  val gameServer = system.actorOf(Props[GameServer], "game-server")

  gameServer
    .ask(IOServer.Bind("openplexus", 16333))(1 second span)
    .onSuccess {
    case IOServer.Bound(endpoint, _) =>
      println("MUDland server started on port 16333")
  }
}

class PlayerHandler extends Actor with ActorLogging {
  val ioBridge = IOExtension(context.system).ioBridge()
  var handle: Option[Connection] = None
  val player = Player("no name")

  def receive = {
    case HandlePlayerAction(buffer) =>
      val tokens = buffer.array.asString.trim.split(" ").toList
      val verb = tokens.head
      val params = tokens.tail ::: List(player.username)
      log.info("params: {}", params)
      player.commands.get(verb).foreach(command => command._2(params))
    case Register(connection, username) =>
      handle = Some(connection)
      player.username = username
  }

  def sendMessage(text: String) = handle.foreach(c => ioBridge ! IOBridge.Send(c, BufferBuilder(text + "\n").toByteBuffer))
}


class Player(var username: String) {
  var name = "no name"
  var attributes = Map[String, Int](
    "vitality" -> 10,
    "strength" -> 10,
    "dexterity" -> 10,
    "intelligence" -> 10
  )
  var commands = mutable.HashMap[String, GameCommands.GameCommand](
    GameCommands.SAY._1 -> GameCommands.SAY,
    GameCommands.SHUTDOWN._1 -> GameCommands.SHUTDOWN
  )
}

object Player {
  def apply(name: String) = new Player(name)
}


object GameCommands {
  type GameCommand = (String, (List[String]) => Unit)

  val SAY = ("say", (params: List[String]) => {
    val stringBuilder = mutable.StringBuilder.newBuilder
    params.reverse.drop(1).reverse.foreach(s => stringBuilder.append(s + " "))
    Mudland.gameServer ! BroadcastMessage(stringBuilder.toString(), params.last)
  })

  val SHUTDOWN = ("shutdown", (params: List[String]) => {
    Mudland.gameServer ! BroadcastMessage("Shutting down...", params.tail.head)
    system.shutdown()
  })
}



