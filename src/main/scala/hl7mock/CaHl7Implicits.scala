package com.eztier.hl7mock

import com.datastax.driver.core.{Row, TypeCodec, UDTValue}

import scala.reflect.runtime.universe._
import com.eztier.cassandra.CaCommon.camelToUnderscores
import com.eztier.cassandra.{CaCustomCodecImplicits, CaDefaultUdtCodec, WithInsertStatement}
import com.eztier.hl7mock.types.{CaHl7, CaHl7Control}

object CaHl7Implicits extends CaCustomCodecImplicits {
  override implicit def toCaCodec[T](innerCodec: TypeCodec[UDTValue])(implicit typeTag: TypeTag[T]) = CaDefaultUdtCodec(innerCodec)

  implicit class WrapCaHl7(el: CaHl7) extends WithInsertStatement {
    override def getInsertStatement(keySpace: String) = {
      val insert = el.insertQuery(keySpace)
      insertValues(insert) values(
        camelToUnderscores("ControlId") -> el.ControlId,
        camelToUnderscores("CreateDate") -> el.CreateDate,
        camelToUnderscores("Id") -> el.Id,
        camelToUnderscores("Message") -> el.Message,
        camelToUnderscores("MessageType") -> el.MessageType,
        camelToUnderscores("Mrn") -> el.Mrn,
        camelToUnderscores("SendingFacility") -> el.SendingFacility
      )
    }
  }

  implicit class WrapCaHl7Control(el: CaHl7Control) extends WithInsertStatement {
    override def getInsertStatement(keySpace: String) = {
      val insert = el.insertQuery(keySpace)
      insertValues(insert) values(
        camelToUnderscores("Id") -> el.Id,
        camelToUnderscores("MessageType") -> el.MessageType,
        camelToUnderscores("CreateDate") -> el.CreateDate
      )
    }
  }

  implicit def rowToCaHl7(row: Row) = {
    CaHl7(
      ControlId = row.getString(camelToUnderscores("ControlId")),
      CreateDate = row.getTimestamp(camelToUnderscores("CreateDate")),
      Id = row.getString(camelToUnderscores("Id")),
      Message = row.getString(camelToUnderscores("Message")),
      MessageType = row.getString(camelToUnderscores("MessageType")),
      Mrn = row.getString(camelToUnderscores("Mrn")),
      SendingFacility = row.getString(camelToUnderscores("SendingFacility"))
    )
  }

  implicit def rowToCaHl7Control(row: Row) = {
    CaHl7Control(
      CreateDate = row.getTimestamp(camelToUnderscores("CreateDate")),
      Id = row.getString(camelToUnderscores("Id")),
      MessageType = row.getString(camelToUnderscores("MessageType"))
    )
  }

}
