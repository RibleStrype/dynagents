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

      override def start(node: MachineNode): Id[MachineNode] = {
        started += 1;
        node
      }

      override def stop(node: MachineNode): Id[MachineNode] = {
        stopped += 1;
        node
      }
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

  it should "request agents when needed" in {
    val mutable = new Mutable(needsAgents)
    import mutable._

    val expected = needsAgents.copy(
      pending = Map(node1 -> time1)
    )

    program.act(needsAgents) shouldBe expected
    started shouldBe 1
    stopped shouldBe 0
  }

  it should "not request agents when pending" in {
    val world = needsAgents.copy(
      pending = Map(node1 -> time1)
    )
    val mutable = new Mutable(world)
    import mutable._

    program.act(world) shouldBe world
    started shouldBe 0
    stopped shouldBe 0
  }

  it should "not shutdown agents if nodes are too young" in {
    val world = WorldView(0, 1, managed, Map(node1 -> time1), Map.empty, time2)
    val mutable = new Mutable(world)
    import mutable._

    program.act(world) shouldBe world
    started shouldBe 0
    stopped shouldBe 0
  }

  it should "shut down agents when there is no backlog and nodes will shortly incur new costs" in {
    val world = WorldView(0, 1, managed, Map(node1 -> time1), Map.empty, time3)
    val mutable = new Mutable(world)
    import mutable._

    val expected = world.copy(
      pending = Map(node1 -> time3)
    )

    program.act(world) shouldBe expected
    started shouldBe 0
    stopped shouldBe 1
  }

  it should "not shut down agents if there are pending actions" in {
    val world = WorldView(0, 1, managed, Map(node1 -> time1), Map(node1 -> time3), time3)
    val mutable = new Mutable(world)
    import mutable._

    program.act(world) shouldBe world
    started shouldBe 0
    stopped shouldBe 0
  }

  it should "shut down agents when there is no backlog if they are too old" in {
    val world = WorldView(0, 1, managed, Map(node1 -> time1), Map.empty, time4)
    val mutable = new Mutable(world)
    import mutable._

    val expected = world.copy(
      pending = Map(node1 -> time4)
    )

    program.act(world) shouldBe expected
    started shouldBe 0
    stopped shouldBe 1
  }

  it should "shut down agents, even if they are potentially doing work, if they are too old" in {
    val world = WorldView(1, 1, managed, Map(node1 -> time1), Map.empty, time4)
    val mutable = new Mutable(world)
    import mutable._

    val expected = world.copy(
      pending = Map(node1 -> time4)
    )

    program.act(world) shouldBe expected
    started shouldBe 0
    stopped shouldBe 1
  }

  it should "ignore unresponsive pending actions during update" in {
    val world = needsAgents.copy(pending = Map(node1 -> time1), time = time2)
    val mutable = new Mutable(world)
    import mutable._

    val expected = world.copy(pending = Map.empty)
    
    program.update(world) shouldBe expected
  }
}
