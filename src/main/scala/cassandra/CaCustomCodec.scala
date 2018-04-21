package com.eztier.cassandra

import java.nio.ByteBuffer

import akka.stream.scaladsl.Flow

import scala.collection.JavaConverters._
import scala.concurrent.{Await, Future, Promise}
import scala.reflect.runtime.universe._
import com.datastax.driver.core._
import com.datastax.driver.core.querybuilder.{Insert}
import com.google.common.reflect.TypeToken
import com.google.common.util.concurrent.FutureCallback
import com.google.common.util.concurrent.Futures
import com.eztier.cassandra.CaCommon.{camelToUnderscores, getFieldNames}

/*
// Example results.
class CaPatientPhoneInfoCodec(innerCodec: TypeCodec[UDTValue], javaType: Class[CaPatientPhoneInfo]) extends TypeCodec[CaPatientPhoneInfo](innerCodec.getCqlType, javaType) {

  val userType: UserType = innerCodec.getCqlType.asInstanceOf

  override def serialize(value: CaPatientPhoneInfo, protocolVersion: ProtocolVersion) = innerCodec.serialize(toUDTValue(value), protocolVersion)

  override def deserialize(bytes: ByteBuffer , protocolVersion: ProtocolVersion) = toCaPatientPhoneInfo(innerCodec.deserialize(bytes, protocolVersion))

  override def format(value: CaPatientPhoneInfo): String =
    if (value == null) null else innerCodec.format(toUDTValue(value))

  override def parse(value: String): CaPatientPhoneInfo =
    if (value == null || value.isEmpty || value.equalsIgnoreCase("NULL")) null else toCaPatientPhoneInfo(innerCodec.parse(value))

  protected def toCaPatientPhoneInfo(value: UDTValue) =
    if (value == null) null
    else CaPatientPhoneInfo(Number = value.getString("number"), Type = value.getString("type"))

  protected def toUDTValue(value: CaPatientPhoneInfo): UDTValue =
    if (value == null) null
    else userType.newValue.setString("number", value.Number).setString("type", value.Type)
}
*/

trait CaCustomCodecImplicits {
  implicit def toCaCodec[T](innerCodec: TypeCodec[UDTValue])(implicit typeTag: TypeTag[T]): CaCodec[_]

  implicit class insertValues(insert: Insert) {
    def values(vals: (String, Any)*) = {
      vals.foldLeft(insert)((i, v) => i.value(v._1, v._2))
    }
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

/*
  '''scala
  // register() will perform the following on the fly if the TypeCodec boilerplats have been created.

  // Retrieve the meta
  val aType: UserType = cluster.getMetadata().getKeyspace(keySpace).getUserType(camelToUnderscores(CaPatientPhoneInfo.getClass.getSimpleName));
  val aTypeCodec: TypeCodec[UDTValue] = cluster.getConfiguration.getCodecRegistry.codecFor(aType);

  // Associate the codec
  val codec = CaPatientPhoneInfoCodec(aTypeCodec, classOf[CaPatientPhoneInfo])

  // Register
  cluster.getConfiguration.getCodecRegistry.register(codec)
  '''
*/

// Default codec
case class CaDefaultUdtCodec(innerCodec: TypeCodec[UDTValue])
  extends TypeCodec[UDTValue](innerCodec.getCqlType, TypeToken.of(classOf[UDTValue]))
    with CaCodec[UDTValue]

case class CaCustomCodecProvider(endpoint: String, keySpace: String, user: String, pass: String) extends CaStreamFlowTask {
  implicit val session = Cluster.builder
    .addContactPoint(endpoint)
    .withPort(9042)
    .withCredentials(user, pass)
    .build
    .connect()

  private val cluster = session.getCluster
  private val registry = cluster.getConfiguration.getCodecRegistry

  implicit def resultSetFutureToScala(f: ResultSetFuture): Future[ResultSet] = {
    val p = Promise[ResultSet]()
    Futures.addCallback(f,
      new FutureCallback[ResultSet] {
        def onSuccess(r: ResultSet) = p success r
        def onFailure(t: Throwable) = p failure t
      })
    p.future
  }

  def register[T <: CaUdt]()(implicit typeTag: TypeTag[T], userImplicits: CaCustomCodecImplicits) = {
    val udtName = camelToUnderscores(typeTag.tpe.typeSymbol.name.toString)

    // Retrieve the meta
    val aType: UserType = cluster.getMetadata().getKeyspace(keySpace).getUserType(udtName)
    val aTypeCodec: TypeCodec[UDTValue] = cluster.getConfiguration.getCodecRegistry.codecFor(aType)

    // Associate the codec, defined by user code within scope.
    import userImplicits._

    val codec: CaCodec[_] = implicitly(aTypeCodec)
    val uCodec = codec.asInstanceOf[TypeCodec[_]]

    // Register
    cluster.getConfiguration.getCodecRegistry.register(uCodec)
    this
  }

  def insertAsync(bs: BoundStatement): Future[ResultSet] = session.executeAsync(bs)

  def readAsync(ss: Statement): Future[ResultSet] = session.executeAsync(ss)

  // Use with caution, will be in lexicon order!
  def toInsertPreparedStatement[T <: CaTbl](el: T) = {
    val fieldNames = getFieldNames(el)
    val fields = fieldNames.map{camelToUnderscores(_)}.mkString(",")
    val placeholder = (1 to fieldNames.length).map(a => "?").mkString(",")

    val stmt = s"""insert into ${keySpace}.${camelToUnderscores(el.getClass.getSimpleName)} ($fields) values($placeholder)"""
    session.prepare(stmt)
  }

  def insertAsync(insert: Insert): Future[ResultSet] = {
    val stmt = new SimpleStatement(insert.toString)
    session.executeAsync(stmt)
  }

  def getInsertFlow() = {
    Flow[Insert].mapAsync(parallelism = 20) {
      insert =>
        val stmt = new SimpleStatement(insert.toString)
        val rs: Future[ResultSet] = session.executeAsync(stmt)
        rs
    }
  }

}
