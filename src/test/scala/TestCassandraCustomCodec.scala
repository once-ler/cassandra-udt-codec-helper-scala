import java.nio.ByteBuffer
import java.{lang, util}

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer

import scala.collection.JavaConverters._
import com.datastax.driver.core._
import org.scalatest.{Matchers, fixture}
import com.eztier.cassandra.CaCustomCodecProvider
import com.eztier.cassandra.CaCommon.{camelToUnderscores, getFieldNames}
import com.google.common.reflect.TypeToken
import epic.patient
import epic.patient.{EmploymentInformation, NameComponents}
import org.h2.expression.Alias
import org.hl7mock.CaPatientImplicits
import org.hl7mock.types._

import scala.concurrent.Future

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
            .register[CaPatientIdType]
            .register[CaPatientNameComponents]
            .register[CaPatientAddress]
            .register[CaPatientCareTeamMember]
            .register[CaPatientEmergencyContact]
            .register[CaPatientEmploymentInformation]

          println("Registered")

          val el = CaPatient(Id = "12345678")

          // Provided by provider.
          val preparedStatement = provider.toInsertPreparedStatement(el)

          // statementBinder provided by user.
          val boundStatement = statementBinder(el, preparedStatement)

          val r = provider.persist(boundStatement)

          println("Binder")

          val stmt1 = new SimpleStatement(s"select * from dwh.${camelToUnderscores(el.getClass.getSimpleName)}").setFetchSize(20)

          val rs = provider.read(stmt1)

          val row: Row = rs.one()

          val addresses = row.getList("addresses", classOf[CaPatientAddress])

          val id = row.getString("id")

          val dt = row.getTimestamp("create_date")

          // val aliases = if (row.isNull(camelToUnderscores("Aliases"))) Seq() else row.getList(camelToUnderscores("Aliases"), classOf[String]).asScala

          val patient = CaPatient(
            Addresses = row.getList(camelToUnderscores("Addresses"), classOf[CaPatientAddress]).asScala,
            Aliases = row.getList(camelToUnderscores("Aliases"), classOf[String]).asScala,
            CareTeam = row.getList(camelToUnderscores("CareTeam"), classOf[CaPatientCareTeamMember]).asScala,
            ConfidentialName = row.getString(camelToUnderscores("ConfidentialName")),
            CreateDate = row.getTimestamp(camelToUnderscores("CreateDate")),
            DateOfBirth = row.getString(camelToUnderscores("DateOfBirth")),
            EmergencyContacts = row.getList(camelToUnderscores("EmergencyContacts"), classOf[CaPatientEmergencyContact]).asScala,
            EmploymentInformation = row.get(camelToUnderscores("EmploymentInformation"), classOf[CaPatientEmploymentInformation]),
            EthnicGroup = row.getString(camelToUnderscores("EthnicGroup")),
            HistoricalIds = row.getList(camelToUnderscores("HistoricalIds"), classOf[CaPatientIdType]).asScala,
            HomeDeployment = row.getString(camelToUnderscores("HomeDeployment")),
            Id = row.getString(camelToUnderscores("Id")),
            Ids = row.getList(camelToUnderscores("Ids"), classOf[CaPatientIdType]).asScala,
            MaritalStatus = row.getString(camelToUnderscores("MaritalStatus")),
            Mrn = row.getString(camelToUnderscores("Mrn")),
            Name = row.getString(camelToUnderscores("Name")),
            NameComponents = row.get(camelToUnderscores("NameComponents"), classOf[CaPatientNameComponents]),
            NationalIdentifier = row.getString(camelToUnderscores("NationalIdentifier")),
            Race = row.getList(camelToUnderscores("Race"), classOf[String]).asScala,
            Rank = row.getString(camelToUnderscores("Rank")),
            Sex = row.getString(camelToUnderscores("Sex")),
            Status = row.getString(camelToUnderscores("Status"))
          )

          println("Simple read")

      }

  }
}
