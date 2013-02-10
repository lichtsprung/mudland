package org.mudland

import akka.actor.ActorSystem

/**
 * Implicit definitions for the game.
 */
object Implicits {
  implicit val system = ActorSystem("mudland-system")
}
