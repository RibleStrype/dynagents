package io.dynagents.jsonformat

import org.typelevel.jawn.{Parser, RawFacade, SimpleFacade}
import scalaz._
import scalaz.Scalaz._

import scala.util.control.NoStackTrace

object JsParser {

  private implicit val facade: RawFacade[JsValue] = new SimpleFacade[JsValue] {
    override def jarray(vs: List[JsValue]): JsValue = JsArray(vs.toIList)
    override def jobject(vs: Map[String, JsValue]): JsValue = JsObject(vs.toList.toIList)
    override def jnull(): JsValue = JsNull
    override def jfalse(): JsValue = JsBoolean(false)
    override def jtrue(): JsValue = JsBoolean(true)
    override def jnum(cs: CharSequence, decIndex: Int, expIndex: Int): JsValue = {
      val s = cs.toString
      val n =
        if (decIndex == -1)
          s.parseLong.map(JsInteger)
        else if (s.endsWith(".0"))
          s.substring(0, s.length - 2).parseLong.map(JsInteger)
        else
          s.parseDouble.map(JsDouble)
      n.getOrElse(
        throw new IllegalArgumentException(s"bad number $s")
          with NoStackTrace // scalafix:ok
      )
    }
    override def jstring(s: CharSequence): JsValue = JsString(s.toString)
  }

  def apply(json: String): String \/ JsValue = Parser.parseFromString(json).toDisjunction.leftMap(_.getMessage)
}
