package com.eztier.test

import org.scalatest.{BeforeAndAfter, Failed, FunSpec, Matchers}

import scala.collection.mutable.ListBuffer
import StarWarsTypes._
import com.datastax.driver.core.ResultSet
import com.datastax.driver.core.querybuilder.QueryBuilder
import com.eztier.cassandra.CaCommon.camelToUnderscores
// import StarWarsImplicits._
import akka.actor.ActorSystem
import akka.actor.Status.Failure
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import com.eztier.cassandra.CaCustomCodecProvider

import scala.concurrent.Await
import scala.concurrent.duration._

class TestCassandraUdtCodecHelperSpec extends FunSpec with Matchers with BeforeAndAfter {
  implicit val system = ActorSystem("Sys")
  implicit val ec = system.dispatcher
  implicit val materializer = ActorMaterializer()

  var movies: ListBuffer[Movie] = ListBuffer[Movie]()

  // Provider will look for user defined implicits.
  implicit val userCodecImplicits = StarWarsCodecImplicits
  import userCodecImplicits._

  val provider = CaCustomCodecProvider("development.cassandra")

  before {
    val newHope = Episode(4, "A New Hope") //1977
    val empire = Episode(5, "The Empire Strikes Back") //1980
    val jedi = Episode(6, "Return of the Jedi") //1983

    val lukeC = Character("1000", "Luke Skywalker", Seq(newHope, empire, jedi))
    val hanC = Character("1002", "Han Solo", Seq(newHope, empire, jedi))
    val leiaC = Character("1003", "Leia Organa", Seq(newHope, empire, jedi))
    val vaderC = Character("1001", "Darth Vader", Seq(newHope, empire, jedi))
    val tarkinC = Character("1004", "Wilhuff Tarkin", Seq(newHope))
    val threepioC = Character("2000", "C-3PO", Seq(newHope, empire, jedi))
    val artooC = Character("2001", "R2-D2", Seq(newHope, empire, jedi))

    val luke = Human(lukeC, Seq(hanC, leiaC, threepioC, artooC), "Tatooine")
    val han = Human(hanC, Seq(lukeC, leiaC, artooC),  "")
    val leia = Human(leiaC, Seq(lukeC, hanC, threepioC, artooC),  "Alderaan")
    val vader = Human(vaderC, Seq(tarkinC), "Tatooine")
    val tarkin = Human(tarkinC, Seq(vaderC), "")
    val threepio = Droid(threepioC, Seq(lukeC, hanC, leiaC, artooC), "Protocol")
    val artoo = Droid(artooC, Seq(lukeC, hanC, leiaC), "Astromech")

    movies += Movie("4", 1977, newHope, Seq(threepio, artoo), Seq(luke, han, leia, vader, tarkin))
    movies += Movie("5", 1980, empire, Seq(threepio, artoo), Seq(luke, han, leia, vader))
    movies += Movie("6", 1983, jedi, Seq(threepio, artoo), Seq(luke, han, leia, vader))

    provider.register[Episode]
      .register[Character]
      .register[Droid]
      .register[Human]
  }

  describe("Cassandra Udt Codec Helper Suite") {

    it("Should infer insert statement with UDT's defined by the user.") {

      val f = Source(movies.toList)
        .map{ _.getInsertStatement("starwars") }
        .via(provider.getInsertFlow)
        .log("Insert Flow")
        .map(Right.apply)
        .recover{
          case _ => Left(Failure)
        }
        .runWith(Sink.head)

      val either = Await.result(f, 10 second)

      // Expect Right(ResultSet[ exhausted: true, Columns[]])
      either should not be ('left)

    }

    it("Should fetch inserted row corrected and unmarshall UDT to scala types.") {

      // Implicitly convert row to Movie type.
      import StarWarsImplicits._

      val s = provider.getSourceStream("select * from starwars.movie where id = '5'")

      val f = s
        .map{
          row =>
            val empire: Movie = row
            Some(empire)
        }
        .log("Transform Row")
        .recover{
          case _ => None
        }
        .runWith(Sink.head)

      val option = Await.result(f, 10 second)

      option should not be (None)

    }

    it("Should construct cql script by recursively iterating through all fields of a table for testing") {
      val el = movies(0)

      val cql = el.toCaString

      println(cql)
    }

  }
}
