package io.dynagents.dda

import io.dynagents.dda.DynAgents.WorldView
import io.dynagents.time.Epoch
import io.dynagents.time.Epoch._
import org.scalatest.{FlatSpec, Matchers}
import scalaz.NonEmptyList
import shapeless.Id

object BusinessLogicSpec {

  object Data {
    val node1 = MachineNode("1243d1af-828f-4ba3-9fc0-a19d86852b5a")
    val node2 = MachineNode("550c4943-229e-47b0-b6be-3d686c5f013f")
    val managed = NonEmptyList(node1, node2)

    val time1: Epoch = epoch"2017-03-03T18:07:00Z"
    val time2: Epoch = epoch"2017-03-03T18:59:00Z" // +52 mins
    val time3: Epoch = epoch"2017-03-03T19:06:00Z" // +59 mins
    val time4: Epoch = epoch"2017-03-03T23:07:00Z" // +5 hours

    val needsAgents = WorldView(5, 0, managed, Map.empty, Map.empty, time1)
  }

  class Mutable(state: WorldView) {
    var started, stopped: Int = 0

    val drone = new Drone[Id] {
      override def getBacklog: Id[Int] = state.backlog
      override def getAgents: Id[Int] = state.agents
    }

    val machines = new Machines[Id] {
      override def getTime: Id[Epoch] = state.time
      override def getManaged: Id[NonEmptyList[MachineNode]] = state.managed
      override def getAlive: Id[Map[MachineNode, Epoch]] = state.alive
      override def start(node: MachineNode): Id[MachineNode] = { started += 1; node }
      override def stop(node: MachineNode): Id[MachineNode] = { stopped += 1; node }
    }

    val program = new DynAgentsModule[Id](drone, machines)
  }

}

class BusinessLogicSpec extends FlatSpec with Matchers {

  import BusinessLogicSpec.Data._
  import BusinessLogicSpec.Mutable

  "BusinessLogic" should "generate an initial world view" in {
    val mutable = new Mutable(needsAgents)
    import mutable._

    program.initial shouldBe needsAgents
  }

  it should "remove changed nodes from pending" in {
    val world = WorldView(0, 0, managed, Map(node1 -> time3), Map.empty, time3)
    val mutable = new Mutable(world)
    import mutable._

    val old = world.copy(
      pending = Map(node1 -> time2),
      alive = Map.empty,
      time = time2
    )
    program.update(old) shouldBe world
  }
}
