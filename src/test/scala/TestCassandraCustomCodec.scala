import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import org.scalatest.{Matchers, fixture}

import com.eztier.cassandra.CaCustomCodec.CaCustomCodecProvider
import org.hl7mock.CaPatientImplicits
import org.hl7mock.types._

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

          val caCustomCodec = CaCustomCodecProvider("127.0.0.1", "keyspace", "cassandra", "abracadabra")

          caCustomCodec.register[CaPatientPhoneInfo]
            .register[CaPatientIdType]
            .register[CaPatientNameComponents]
            .register[CaPatientAddress]
            .register[CaPatientCareTeamMember]
            .register[CaPatientEmergencyContact]
            .register[CaPatientEmploymentInformation]

          println("Registered")
      }

  }
}
