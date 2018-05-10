package com.eztier.hl7mock

import com.datastax.driver.core.querybuilder.QueryBuilder
import com.datastax.driver.core.{TypeCodec, UDTValue}
import scala.reflect.runtime.universe._

import com.eztier.cassandra.CaCommon.camelToUnderscores
import com.eztier.cassandra.{CaCustomCodecImplicits, CaDefaultUdtCodec}
import com.eztier.hl7mock.types.{CaHl7, CaHl7Control}

object CaHl7Implicits extends CaCustomCodecImplicits {
  override implicit def toCaCodec[T](innerCodec: TypeCodec[UDTValue])(implicit typeTag: TypeTag[T]) = CaDefaultUdtCodec(innerCodec)

  def insertStatement[T](keySpace: String, in: T)(implicit typeTag: TypeTag[T]) = {
    val t = camelToUnderscores(in.getClass.getSimpleName)
    val insert = QueryBuilder.insertInto(keySpace, t)

    in match {
      case el: CaHl7 => insertValues(insert) values(
        camelToUnderscores("ControlId") -> el.ControlId,
        camelToUnderscores("CreateDate") -> el.CreateDate,
        camelToUnderscores("Id") -> el.Id,
        camelToUnderscores("Message") -> el.Message,
        camelToUnderscores("MessageType") -> el.MessageType,
        camelToUnderscores("Mrn") -> el.Mrn,
        camelToUnderscores("SendingFacility") -> el.SendingFacility
      )
      case el: CaHl7Control => insertValues(insert) values(
        camelToUnderscores("Id") -> el.Id,
        camelToUnderscores("MessageType") -> el.MessageType,
        camelToUnderscores("CreateDate") -> el.CreateDate
      )
      case _ => insertValues(insert) values()
    }
  }
}
