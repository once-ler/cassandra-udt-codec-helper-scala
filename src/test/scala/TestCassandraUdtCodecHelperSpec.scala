package test

import org.scalatest.{BeforeAndAfter, FunSpec, Matchers}

import scala.collection.mutable.ListBuffer
import StarWarsTypes._
import StarWarsImplicits._
import akka.Done
import akka.stream.scaladsl.{Sink, Source}
import com.eztier.cassandra.CaCustomCodecProvider

import scala.concurrent.Await
import scala.concurrent.duration._

class TestCassandraUdtCodecHelperSpec extends FunSpec with Matchers with BeforeAndAfter {
  val newHope = Episode(4, "A New Hope") //1977
  val empire = Episode(4, "The Empire Strikes Back") //1980
  val jedi = Episode(4, "Return of the Jedi") //1983

  val luke = Human("1000", "Luke Skywalker", List(newHope, empire, jedi), List(han, leia, threepio, artoo), "Tatooine")
  val han = Human("1002", "Han Solo", List(newHope, empire, jedi), List(luke, leia, artoo),  "")
  val leia = Human("1003", "Leia Organa", List(newHope, empire, jedi), List(luke, han, threepio, artoo),  "Alderaan")

  val vader = Human("1001", "Darth Vader", List(newHope, empire, jedi), List(tarkin), "Tatooine")
  val tarkin = Human("1004", "Wilhuff Tarkin", List(newHope), List(vader), "")

  val threepio = Droid("2000", "C-3PO", List(newHope, empire, jedi), List(luke, han, leia, artoo), "Protocol")
  val artoo = Droid("2000", "C-3PO", List(newHope, empire, jedi), List(luke, han, leia), "Astromech")

  var movies: ListBuffer[Movie] = _

  // Provider will look for user defined implicits.
  implicit val userImplicits = StarWarsImplicits
  import userImplicits._

  val provider = CaCustomCodecProvider("development.cassandra")

  before {
    movies += Movie("4", 1977, newHope, List(threepio, artoo), List(luke, han, leia, vader, tarkin))
    movies += Movie("5", 1980, newHope, List(threepio, artoo), List(luke, han, leia, vader))
    movies += Movie("6", 1983, newHope, List(threepio, artoo), List(luke, han, leia, vader))

    provider.register[Episode]
      .register[Character]
      .register[Droid]
      .register[Human]
  }

  describe("") {

    it("Should infer insert statement with UDT's defined by the user.") {

      val f = Source(movies.toList)
        .mapAsync(3){ _.getInsertStatement("starwars") }
        .via(provider.getInsertFlow)
        .runWith(Sink.ignore)

      val r = Await.result(f, 10 second)

      r == Done
    }

    it("Should fetch inserted row corrected and unmarshall UDT to scala types.") {

    }

  }
}
