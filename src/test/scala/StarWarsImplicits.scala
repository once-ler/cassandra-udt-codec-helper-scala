package test

import com.eztier.cassandra.CaCustomCodecImplicits
import scala.collection.JavaConverters._
import scala.reflect.runtime.universe._
import com.datastax.driver.core._
import com.google.common.reflect.TypeToken
import com.eztier.cassandra.CaCommon.{camelToUnderscores, getFieldNames}
import com.eztier.cassandra._

import StarWarsTypes._

object StarWarsImplicits extends CaCustomCodecImplicits {
  // Override implicit conversion function.
  override implicit def toCaCodec[T](innerCodec: TypeCodec[UDTValue])(implicit typeTag: TypeTag[T]) = {
    typeTag.tpe match {
      case a if a == typeOf[Episode] => EpisodeCodec(innerCodec)
      case a if a == typeOf[Character] => CharacterCodec(innerCodec)
      case a if a == typeOf[Droid] => DroidCodec(innerCodec)
      case a if a == typeOf[Human] => HumanCodec(innerCodec)

      case _ => CaDefaultUdtCodec(innerCodec)
    }
  }

  implicit class WrapMovies(el: Movie) extends WithInsertStatement {
    override def getInsertStatement(keySpace: String) = {
      val insert = el.insertQuery(keySpace)
      insertValues(insert) values(
        camelToUnderscores("Id") -> el.Id,
        camelToUnderscores("YearReleased") -> el.YearReleased,
        camelToUnderscores("EpisodeReleased") -> el.EpisodeReleased,
        camelToUnderscores("Droids") -> el.Droids.asJava,
        camelToUnderscores("Humans") -> el.Humans.asJava
      )
    }
  }

  // Convert Droid or Human to Character type.
  implicit def toCharacter[T <: WithCharacter with WithFriends](el: T)= Character(el.Id, el.Name, el.AppearsIn)

  implicit def rowToMovie(row: Row) =
    Movie(
      Id = row.getString(camelToUnderscores("Id")),
      YearReleased = row.getInt(camelToUnderscores("YearReleased")),
      EpisodeReleased = row.get(camelToUnderscores("EpisodeReleased"), classOf[Episode]),
      Droids = row.getList(camelToUnderscores("Droids"), classOf[Droid]).asScala,
      Humans = row.getList(camelToUnderscores("Humans"), classOf[Human]).asScala
    )

  // Extend TypeCodec and define custom encode/decode formats below:

  // Episode
  case class EpisodeCodec(innerCodec: TypeCodec[UDTValue])
    extends TypeCodec[Episode](innerCodec.getCqlType, TypeToken.of(classOf[Episode]))
      with CaCodec[Episode] {

    override def toCaClass(value: UDTValue) =
      if (value == null) null
      else Episode(Id = value.getByte("id"), Name = value.getString("name"))

    override def toUDTValue(value: Episode): UDTValue =
      if (value == null) null
      else userType.newValue.setByte("id", value.Id).setString("name", value.Name)
  }

  // Character
  case class CharacterCodec(innerCodec: TypeCodec[UDTValue])
    extends TypeCodec[Character](innerCodec.getCqlType, TypeToken.of(classOf[Character]))
      with CaCodec[Character] {

    override def toCaClass(value: UDTValue) =
      if (value == null) null
      else new Character(
        Id = value.getString("id"),
        Name = value.getString("name"),
        AppearsIn = value.getList(camelToUnderscores("AppearsIn"), TypeToken.of(classOf[Episode])).asScala
      )

    override def toUDTValue(value: Character): UDTValue =
      if (value == null) null
      else userType.newValue
        .setString("id", value.Id)
        .setString("name", value.Name)
        .setList(camelToUnderscores("AppearsIn"), value.AppearsIn.asJava)
  }

  // Droid
  case class DroidCodec(innerCodec: TypeCodec[UDTValue])
    extends TypeCodec[Droid](innerCodec.getCqlType, TypeToken.of(classOf[Droid]))
      with CaCodec[Droid] {

    override def toCaClass(value: UDTValue) =
      if (value == null) null
      else new Droid(
        Id = value.getString("id"),
        Name = value.getString("name"),
        AppearsIn = value.getList(camelToUnderscores("AppearsIn"), TypeToken.of(classOf[Episode])).asScala,
        Friends = value.getList(camelToUnderscores("Friends"), TypeToken.of(classOf[Character])).asScala,
        PrimaryFunction = value.getString("primary_function")
      )

    override def toUDTValue(value: Droid): UDTValue =
      if (value == null) null
      else userType.newValue
        .setString("id", value.Id)
        .setString("name", value.Name)
        .setList(camelToUnderscores("AppearsIn"), value.AppearsIn.asJava)
        .setList(camelToUnderscores("Friends"), value.Friends.asJava)
        .setString("primary_function", value.Name)
  }

  // Human
  case class HumanCodec(innerCodec: TypeCodec[UDTValue])
    extends TypeCodec[Human](innerCodec.getCqlType, TypeToken.of(classOf[Human]))
      with CaCodec[Human] {

    override def toCaClass(value: UDTValue) =
      if (value == null) null
      else new Human(
        Id = value.getString("id"),
        Name = value.getString("name"),
        AppearsIn = value.getList(camelToUnderscores("AppearsIn"), TypeToken.of(classOf[Episode])).asScala,
        Friends = value.getList(camelToUnderscores("Friends"), TypeToken.of(classOf[Character])).asScala,
        HomePlanet = value.getString("home_planet")
      )

    override def toUDTValue(value: Human): UDTValue =
      if (value == null) null
      else userType.newValue
        .setString("id", value.Id)
        .setString("name", value.Name)
        .setList(camelToUnderscores("AppearsIn"), value.AppearsIn.asJava)
        .setList(camelToUnderscores("Friends"), value.Friends.asJava)
        .setString("home_planet", value.Name)
  }

}
