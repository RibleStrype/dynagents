package io.dynagents.dda

import io.dynagents.time.Epoch
import scalaz.NonEmptyList

trait Drone[F[_]] {
  def getBacklog: F[Int]
  def getAgents: F[Int]
}

final case class MachineNode(id: String)
trait Machines[F[_]] {
  def getTime: F[Epoch]
  def getManaged: F[NonEmptyList[MachineNode]]
  def getAlive: F[Map[MachineNode, Epoch]]
  def start(node: MachineNode): F[Unit]
  def stop(node: MachineNode): F[Unit]
}