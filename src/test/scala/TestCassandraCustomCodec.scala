import java.nio.ByteBuffer
import java.{lang, util}

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer

import scala.collection.JavaConverters._
import com.datastax.driver.core.{Duration, _}
import org.scalatest.{Matchers, fixture}
import com.eztier.cassandra.CaCustomCodecProvider
import com.eztier.cassandra.CaCommon.{camelToUnderscores, getFieldNames}
import com.google.common.reflect.TypeToken
import epic.patient
import epic.patient.{EmploymentInformation, NameComponents}
import org.h2.expression.Alias
import org.hl7mock.CaPatientImplicits
import org.hl7mock.types._

import scala.concurrent.{Await, Future}

class TestCassandraCustomSpec extends fixture.FunSpec with Matchers with fixture.ConfigMapFixture {

  implicit val system = ActorSystem("Sys")
  implicit val ec = system.dispatcher
  implicit val materializer = ActorMaterializer()
  implicit val logger = system.log

  def withSearchCriteria(test: Option[String] => Any): Unit = {
    test(Some("EBF771B0B8A10A43A39C3CE8EF4D7F18"))
  }

  it("Can create a UDT codec on the fly") {
    () =>
      withSearchCriteria {
        cri =>

          // User defined implicits.
          implicit val userImplicits = CaPatientImplicits

          import userImplicits._

          val provider = CaCustomCodecProvider("127.0.0.1", "keyspace", "cassandra", "abracadabra")

          provider.register[CaPatientPhoneInfo]
            .register[CaPatientEmailInfo]
            .register[CaPatientIdType]
            .register[CaPatientNameComponents]
            .register[CaPatientAddress]
            .register[CaPatientCareTeamMember]
            .register[CaPatientEmergencyContact]
            .register[CaPatientEmploymentInformation]

          println("Registered")

          val el = CaPatient(Id = "12345678")

          // preparedStatement provided by user.
          implicit val session = provider.session
          val preparedStatement = getPreparedStatement("dwh", el)

          // statementBinder provided by user.
          val boundStatement = getStatementBinder(el, preparedStatement)

          import scala.concurrent.duration.Duration
          val fut0 = provider.insertAsync(boundStatement)
          val res = Await.result(fut0, Duration.Inf)

          println("Binder")

          val insertStatement = el getInsertStatement("dwh")
          val qs = insertStatement.getQueryString()
          val stmt0 = new SimpleStatement(insertStatement.toString)

          println("Insert Statement")

          val stmt1 = new SimpleStatement(s"select * from dwh.${camelToUnderscores(el.getClass.getSimpleName)}").setFetchSize(20)

          val fut = provider.readAsync(stmt1)
          val rs = Await.result(fut, Duration.Inf)

          val row: Row = rs.one()

          val addresses = row.getList("addresses", classOf[CaPatientAddress])

          val id = row.getString("id")

          val dt = row.getTimestamp("create_date")

          val patient: CaPatient = row

          println("Simple read")

      }

  }
}
