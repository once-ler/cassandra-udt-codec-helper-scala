package com.eztier.cassandra

import java.net.{InetAddress, InetSocketAddress}

import akka.stream.scaladsl.Flow

import scala.concurrent.{Future, Promise}
import scala.reflect.runtime.universe._
import com.datastax.driver.core._
import com.datastax.driver.core.querybuilder.Insert
import com.google.common.reflect.TypeToken
import com.google.common.util.concurrent.FutureCallback
import com.google.common.util.concurrent.Futures
import com.eztier.cassandra.CaCommon.{camelToUnderscores, getFieldNames}

import scala.collection.JavaConverters._

// Default codec
case class CaDefaultUdtCodec(innerCodec: TypeCodec[UDTValue])
  extends TypeCodec[UDTValue](innerCodec.getCqlType, TypeToken.of(classOf[UDTValue]))
    with CaCodec[UDTValue]

case class CaCustomCodecProvider(endpoints: java.util.Collection[InetAddress], keySpace: String, user: String, pass: String, port: Int = 9042) extends CaStreamFlowTask {
  implicit lazy val session = Cluster.builder
    .addContactPoints(endpoints)
    .withPort(port)
    .withCredentials(user, pass)
    .build
    .connect(keySpace)

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

// Note: must match datastax configuration
object CaCustomCodecProvider {
  def apply(path: String) = {
    import com.typesafe.config._
    val conf = ConfigFactory.load()

    if (!conf.hasPath(path))
      throw new IllegalArgumentException(s"No configuration setting found for path $path")

    val config = conf.getConfig(path)
    val keySpace = config.getString("keyspace")
    val sessionConf = config.getConfig("session")
    val contactPoint =
      if (sessionConf.hasPath("contactPoints"))
        sessionConf.getString("contactPoint")
      else "127.0.0.1"
    val port =
      if (sessionConf.hasPath("port"))
        sessionConf.getInt("port")
      else 9042
    val cred = sessionConf.getStringList("credentials").asScala
    val endpoints = sessionConf.hasPath("contactPoints") match {
      case true => sessionConf.getStringList("contactPoints").asScala
      case _ => Seq(contactPoint)
    }

    val contactPoints = endpoints.map {
      a =>
        if (a.contains(":")) {
          val addr = a.split(":")
          new InetSocketAddress(addr(0), addr(1).toInt).getAddress
        } else new InetSocketAddress(a, port).getAddress
    }

    new CaCustomCodecProvider(contactPoints.asJavaCollection, keySpace, cred(0), cred(1), port)
  }
}

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
