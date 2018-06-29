package com.eztier.test

import com.eztier.cassandra.{CaTbl, CaUdt}

object StarWarsTypes {
  case class Episode (
    Id: Byte,
    Name: String
  ) extends CaUdt

  case class Character (
    Id: String,
    Name: String,
    AppearsIn: Seq[Episode]
  ) extends CaUdt

  trait WithCharacter {
    def Id: String
    def Name: String
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
  ) extends WithCharacter with WithFriends with CaUdt

  object Droid {
    def apply(c: Character, f: Seq[Character], p: String) = new Droid(c.Id, c.Name, c.AppearsIn, f, p)
  }

  case class Human (
    Id: String,
    Name: String,
    AppearsIn: Seq[Episode],
    Friends: Seq[Character],
    HomePlanet: String
  ) extends WithCharacter with WithFriends with CaUdt

  object Human {
    def apply(c: Character, f: Seq[Character], h: String) = new Human(c.Id, c.Name, c.AppearsIn, f, h)
  }

  case class Movie (
    Id: String,
    YearReleased: Int,
    EpisodeReleased: Episode,
    Droids: Seq[Droid],
    Humans: Seq[Human]
  ) extends CaTbl

  object Movie {
    def unapply(arg: Movie): Option[(String, Int, Episode, Seq[Droid], Seq[Human])] =
      Some(arg.Id, arg.YearReleased, arg.EpisodeReleased, arg.Droids, arg.Humans)
  }
}
