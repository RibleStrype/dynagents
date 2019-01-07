package io.dynagents.jsonformat

import scalaz.Scalaz._
import scalaz._
import simulacrum.typeclass

sealed abstract class JsValue

final case object JsNull extends JsValue

final case class JsObject(fields: IList[(String, JsValue)]) extends JsValue

final case class JsArray(elements: IList[JsValue]) extends JsValue

final case class JsBoolean(value: Boolean) extends JsValue

final case class JsString(value: String) extends JsValue

final case class JsDouble(value: Double) extends JsValue

final case class JsInteger(value: Long) extends JsValue

@typeclass trait JsEncoder[A] {
  def toJson(obj: A): JsValue
}

@typeclass trait JsDecoder[A] {
  def fromJson(json: JsValue): String \/ A
}

object JsDecoder {
  implicit val string: JsDecoder[String] = {
    case JsString(value) => \/-(value)
    case _               => -\/("not a string")
  }

  implicit val long: JsDecoder[Long] = {
    case JsInteger(value) => \/-(value)
    case _                => -\/("not a long")
  }
}

object JsValue {

  implicit class Ops(j: JsValue) {
    def getAs[A: JsDecoder](key: String): String \/ A = {
      val v = j match {
        case o: JsObject => o.fields.collectFirst { case (`key`, value) => value }
        case _           => None
      }
      v.\/>(s"no key [$key] found on $j")
        .flatMap(JsDecoder[A].fromJson)
        .leftMap(err => s"error parsing value at key [$key]: $err")
    }

    def as[A: JsDecoder]: String \/ A =
      JsDecoder[A].fromJson(j)
  }

}