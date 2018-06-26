package test

import java.util.Date

object StarWarsTypes {
  case class Episode (
    Id: Byte,
    Name: String
  )

  case class Character (
    Id: String,
    Name: String,
    AppearsIn: Seq[Episode]
  )

  trait WithCharacter {
    def Id: String,
    def Name: String,
    def AppearsIn: Seq[Episode]
  }

  trait WithFriends {
    def Friends: Seq[Character]
  }

  case class Droid (
    Id: String,
    Name: String,
    AppearsIn: Seq[Episode],
    Friends: Seq[Character],
    PrimaryFunction: String
  ) extends WithCharacter with WithFriends

  case class Human (
    Id: String,
    Name: String,
    AppearsIn: Seq[Episode],
    Friends: Seq[Character],
    HomePlanet: String
  ) extends WithCharacter with WithFriends

  case class Movie (
    Id: String,
    YearReleased: Int,
    EpisodeReleased: Episode,
    Droids: Seq[Droid],
    Humans: Seq[Human]
  )
}
