package io.dynagents.time

trait LocalClock[F[_]] {
  def now: F[Epoch]
}
