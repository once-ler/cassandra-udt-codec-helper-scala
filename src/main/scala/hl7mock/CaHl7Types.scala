package com.eztier.hl7mock.types

import java.util.Date

case class CaHl7(
  ControlId: String = "",
  CreateDate: Date = new Date(),
  Id: String = "",
  Message: String = "",
  MessageType: String = "",
  Mrn: String = "",
  SendingFacility: String = ""
)

case class CaHl7Control(
  Id: String = "",
  MessageType: String = "",
  CreateDate: Date = new Date()
)
