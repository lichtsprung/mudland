package org.mudland

import spray.io._
import org.mudland.PlayerRegistry._
import org.mudland.PlayerRegistry.PlayerNameRequest
import org.mudland.PlayerRegistry.PlayerConnectionResponse
import org.mudland.PlayerRegistry.PlayerNameChangeRequest
import org.mudland.PlayerRegistry.PlayerNameResponse
import collection.mutable
import akka.actor.{Actor, ActorLogging}

/**
 * The global player registry.
 * TODO probably isn't needed anymore
 */
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