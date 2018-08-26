package com.eztier.test

import scala.collection.JavaConverters._
import com.datastax.driver.core._
import com.eztier.cassandra.CaCommon.{camelToUnderscores}

import StarWarsTypes._

object StarWarsImplicits {

  // Convert Droid or Human to Character type.
  // implicit def toCharacter[T <: WithCharacter with WithFriends](el: T)= Character(el.Id, el.Name, el.AppearsIn)

  implicit def rowToMovie(row: Row) =
    Movie(
      Id = row.getString(camelToUnderscores("Id")),
      YearReleased = row.getInt(camelToUnderscores("YearReleased")),
      EpisodeReleased = row.get(camelToUnderscores("EpisodeReleased"), classOf[Episode]),
      Droids = row.getList(camelToUnderscores("Droids"), classOf[Droid]).asScala,
      Humans = row.getList(camelToUnderscores("Humans"), classOf[Human]).asScala
    )

}
