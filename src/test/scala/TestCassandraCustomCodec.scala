import java.nio.ByteBuffer
import java.{lang, util}

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer

import scala.collection.JavaConverters._
import com.datastax.driver.core._
import org.scalatest.{Matchers, fixture}
import com.eztier.cassandra.CaCustomCodecProvider
import com.eztier.cassandra.CaCommon.{camelToUnderscores, getFieldNames}
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

          val provider = CaCustomCodecProvider("127.0.0.1", "keyspace", "cassandra", "abracadabra")

          provider.register[CaPatientPhoneInfo]
            .register[CaPatientIdType]
            .register[CaPatientNameComponents]
            .register[CaPatientAddress]
            .register[CaPatientCareTeamMember]
            .register[CaPatientEmergencyContact]
            .register[CaPatientEmploymentInformation]

          println("Registered")

          val statementBinder = (el: CaPatient, statement: PreparedStatement) => {

            statement.bind(
              el.Addresses.asJava,
              el.Aliases.asJava,
              el.CareTeam.asJava,
              el.ConfidentialName,
              el.CreateDate,
              el.DateOfBirth,
              el.EmergencyContacts.asJava,
              el.EmploymentInformation,
              el.EthnicGroup,
              el.HistoricalIds.asJava,
              el.HomeDeployment,
              el.Id,
              el.Ids.asJava,
              el.MaritalStatus,
              el.Mrn,
              el.Name,
              el.NameComponents,
              el.NationalIdentifier,
              el.Race.asJava,
              el.Rank,
              el.Sex,
              el.Status
            )

          }

          val el = CaPatient(Id = "12345678")
          val fields = getFieldNames(el).map{camelToUnderscores(_)}.mkString(",")
          val placeholder = getFieldNames(el).map{a => "?"}.mkString(",")

          val stmt = s"""insert into dwh.${camelToUnderscores(el.getClass.getSimpleName)} ($fields) values($placeholder)"""

          val boundStatement = statementBinder(el, provider.session.prepare(stmt))

          val r = provider.persist(boundStatement)

          println("Binder")

          val stmt1 = new SimpleStatement(s"select * from dwh.${camelToUnderscores(el.getClass.getSimpleName)}").setFetchSize(20)

          val rs = provider.read(stmt1)

          val row = rs.one()

          val addresses = row.getList("addresses", classOf[CaPatientAddress])

          val id = row.getString("id")

          val dt = row.getTimestamp("create_date")

          println("Simple read")

      }

  }
}
