package org.mudland

import akka.actor.ActorSystem
import java.nio.ByteBuffer

/**
 * Implicit definitions for the game.
 */
object Implicits {
  implicit val system = ActorSystem("mudland-system")
}
