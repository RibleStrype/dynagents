package io.dynagents.dda

import io.dynagents.dda.DynAgents.{NeedsAgent, Stale, WorldView}
import io.dynagents.time.Epoch
import scalaz._, Scalaz._

import scala.concurrent.duration._

trait DynAgents[F[_]] {
  def initial: F[WorldView]

  def update(old: WorldView): F[WorldView]

  def act(world: WorldView): F[WorldView]
}

object DynAgents {

  final case class WorldView(
    backlog: Int,
    agents: Int,
    managed: NonEmptyList[MachineNode],
    alive: Map[MachineNode, Epoch],
    pending: Map[MachineNode, Epoch],
    time: Epoch
  )

  object NeedsAgent {
    def unapply(world: WorldView): Option[MachineNode] = world match {
      case WorldView(backlog, 0, managed, alive, pending, _)
        if backlog > 0 && alive.isEmpty && pending.isEmpty => Option(managed.head)
      case _                                               => None
    }
  }

  object Stale {
    def unapply(world: WorldView): Option[NonEmptyList[MachineNode]] = world match {
      case WorldView(backlog, _, _, alive, pending, time) if alive.nonEmpty =>
        (alive -- pending.keySet).collect {
          case (n, started) if backlog == 0 && (time - started).toMinutes % 60 >= 58 => n
          case (n, started) if (time - started) >= 5.hours                           => n
        }.toList.toNel
      case _                                                                => None
    }
  }

}

class DynAgentsModule[F[_] : Monad](D: Drone[F], M: Machines[F]) extends DynAgents[F] {
  override def initial: F[WorldView] = for {
    db <- D.getBacklog
    da <- D.getAgents
    mm <- M.getManaged
    ma <- M.getAlive
    mt <- M.getTime
  } yield WorldView(
    backlog = db,
    agents = da,
    managed = mm,
    alive = ma,
    pending = Map.empty,
    time = mt
  )

  override def update(old: WorldView): F[WorldView] = for {
    snap <- initial
    changed = symdiff(old.alive.keySet, snap.alive.keySet)
    pending = (old.pending -- changed).filterNot {
      case (_, started) => snap.time - started >= 10.minutes
    }
    update = snap.copy(pending = pending)
  } yield update

  override def act(world: WorldView): F[WorldView] = world match {
    case NeedsAgent(node) => for {
      _ <- M.start(node)
      update = world.copy(pending = world.pending.updated(node, world.time))
    } yield update

    case Stale(nodes) => nodes.foldLeftM(world) { (w, n) =>
      for {
        _ <- M.stop(n)
        update = w.copy(pending = w.pending.updated(n, w.time))
      } yield update
    }

    case _ => world.pure[F]
  }

  private def symdiff[T](a: Set[T], b: Set[T]): Set[T] =
    (a union b) -- (a intersect b)
}
