package com.eztier.hl7mock

import com.datastax.driver.core.querybuilder.QueryBuilder

import scala.reflect.runtime.universe._
import com.datastax.driver.core.{Row, TypeCodec, UDTValue}
import com.eztier.cassandra.CaCommon.camelToUnderscores
import com.eztier.cassandra.{CaCustomCodecImplicits, CaDefaultUdtCodec, WithInsertStatement}
import com.eztier.hl7mock.types.{CaHl7Control, CaTableDateControl}

object CaCommonImplicits extends CaCustomCodecImplicits {
  override implicit def toCaCodec[T](innerCodec: TypeCodec[UDTValue])(implicit typeTag: TypeTag[T]) = CaDefaultUdtCodec(innerCodec)

  implicit class WrapCaTableDateControl(el: CaTableDateControl) extends WithInsertStatement {
    override def getInsertStatement(keySpace: String) = {
      val insert = el.insertQuery(keySpace)
      insertValues(insert) values(
        camelToUnderscores("Id") -> el.Id,
        camelToUnderscores("CreateDate") -> el.CreateDate
      )
    }
  }

  implicit def rowToCaTableDateControl(row: Row) = {
    CaTableDateControl(
      CreateDate = row.getTimestamp(camelToUnderscores("CreateDate")),
      Id = row.getString(camelToUnderscores("Id"))
    )
  }
}

