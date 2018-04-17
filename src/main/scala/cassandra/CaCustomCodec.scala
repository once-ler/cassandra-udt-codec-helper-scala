package com.eztier.cassandra

import java.nio.ByteBuffer

import scala.concurrent.{Future, Promise, Await}
import scala.reflect.runtime.universe._
import com.datastax.driver.core._
import com.google.common.reflect.TypeToken
import com.google.common.util.concurrent.FutureCallback
import com.google.common.util.concurrent.Futures

import com.eztier.cassandra.CaCommon.camelToUnderscores


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

object CaCustomCodec {


}

case class CaCustomCodecProvider(endpoint: String, keySpace: String, user: String, pass: String) {
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

  def persist(bs: BoundStatement) = {
    import scala.concurrent.duration._

    val rs: Future[ResultSet] = session.executeAsync(bs)

    Await.result(rs, Duration.Inf)
  }

  def read(ss: Statement) = {
    import scala.concurrent.duration._

    val rs: Future[ResultSet] = session.executeAsync(ss)

    Await.result(rs, Duration.Inf)
  }

}
