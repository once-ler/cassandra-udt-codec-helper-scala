package com.eztier.cassandra

import java.nio.ByteBuffer
import scala.reflect.runtime.universe._
import com.datastax.driver.core.{ProtocolVersion, TypeCodec, UDTValue, UserType}
import com.datastax.driver.core.querybuilder.{Insert, QueryBuilder}
import com.eztier.cassandra.CaCommon.camelToUnderscores

trait WithInsertStatement {
  def getInsertStatement(keySpace: String): Insert
}

trait CaCustomCodecImplicits {
  implicit def toCaCodec[T](innerCodec: TypeCodec[UDTValue])(implicit typeTag: TypeTag[T]): CaCodec[_]

  implicit class insertValues(insert: Insert) {
    def values(vals: (String, Any)*) = {
      vals.foldLeft(insert)((i, v) => i.value(v._1, v._2))
    }
  }

  implicit class GetQueryBuilderInsert[T](in: T) {
    def insertQuery(keySpace: String) = QueryBuilder.insertInto(keySpace, camelToUnderscores(in.getClass.getSimpleName))
  }
}

trait CaCodec[T] {
  implicit val innerCodec: TypeCodec[UDTValue]
  val userType: UserType = innerCodec.getCqlType().asInstanceOf[UserType]

  def serialize(value: T, protocolVersion: ProtocolVersion) = innerCodec.serialize(toUDTValue(value), protocolVersion)

  def deserialize(bytes: ByteBuffer , protocolVersion: ProtocolVersion) = toCaClass(innerCodec.deserialize(bytes, protocolVersion))

  def format(value: T): String =
    if (value == null) null else innerCodec.format(toUDTValue(value))

  def parse(value: String): T =
    if (value == null || value.isEmpty || value.equalsIgnoreCase("NULL")) null.asInstanceOf[T] else toCaClass(innerCodec.parse(value))

  def toCaClass(value: UDTValue): T = null.asInstanceOf[T]

  def toUDTValue(value: T): UDTValue = null
}
