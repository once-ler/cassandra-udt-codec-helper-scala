package com.eztier.hl7mock.types

import java.text.SimpleDateFormat
import java.util.Date

import com.eztier.cassandra.{CaTbl, CaUdt}
import org.joda.time.DateTime

case class CaPatientPhoneInfo (
  Number: String = "",
  Type: String = ""
) extends CaUdt

case class CaPatientEmailInfo (
  Email: String = "",
  Type: String = ""
) extends CaUdt

case class CaPatientIdType (
 Id: String = "",
 Type: String = ""
) extends CaUdt

case class CaPatientNameComponents(
  Academic: String = "",
  FirstName: String = "",
  GivenName: String = "",
  Initials: String = "",
  LastName: String = "",
  LastNameFromSpouse: String = "",
  LastNamePrefix: String = "",
  MiddleName: String = "",
  PreferredName: String = "",
  PreferredNameType: String = "",
  SpouseLastNameFirst: String = "",
  SpouseLastNamePrefix: String = "",
  Suffix: String = "",
  Title: String = ""
) extends CaUdt

case class CaPatientAddress (
  City: String = "",
  Country: String = "",
  County: String = "",
  District: String = "",
  Email: Seq[CaPatientEmailInfo] = Seq(),
  HouseNumber: String = "",
  PhoneNumbers: Seq[CaPatientPhoneInfo] = Seq(),
  PostalCode: String = "",
  State: String = "",
  Street: Seq[String] = Seq(),
  Type: String = ""
) extends CaUdt

case class CaPatientCareTeamMember (
  Ids: Seq[CaPatientIdType] = Seq(),
  Name: String = "",
  Type: String = ""
) extends CaUdt

case class CaPatientEmergencyContact(
  LegalGuardian: String = "",
  Name: String = "",
  PhoneNumbers: Seq[CaPatientPhoneInfo] = Seq(),
  Relation: String = ""
) extends CaUdt

case class CaPatientEmploymentInformation(
  EmployerName: String = "",
  Occupation: String = "",
  PhoneNumbers: Seq[CaPatientPhoneInfo] = Seq()
) extends CaUdt

case class CaPatient(
  Addresses: Seq[CaPatientAddress] = Seq(),
  Aliases: Seq[String] = Seq(),
  CareTeam: Seq[CaPatientCareTeamMember] = Seq(),
  ConfidentialName: String = "",
  CreateDate: Date = new Date(),
  DateOfBirth: Date = new DateTime(1900, 1, 1, 0, 0, 0).toDate,
  EmergencyContacts: Seq[CaPatientEmergencyContact] = Seq(),
  EmploymentInformation: CaPatientEmploymentInformation = CaPatientEmploymentInformation(),
  Ethnicity: Seq[String] = Seq(),
  Gender: String = "",
  HistoricalIds: Seq[CaPatientIdType] = Seq(),
  HomeDeployment: String = "",
  Id: String = "",
  Ids: Seq[CaPatientIdType] = Seq(),
  MaritalStatus: String = "",
  Mrn: String = "",
  Name: String = "",
  NameComponents: Seq[CaPatientNameComponents] = Seq(),
  NationalIdentifier: String = "",
  Race: Seq[String] = Seq(),
  Rank: String = "",
  Status: String = ""
) extends CaTbl

case class CaPatientControl(
  CreateDate: Date = new Date(),
  City: String = "",
  DateOfBirth: Date = new Date(),
  Email: String = "",
  Ethnicity: String = "",
  Gender: String = "",
  Id: String = "",
  Mrn: String = "",
  Name: String = "",
  PhoneNumber: String = "",
  PostalCode: String = "",
  Race: String = "",
  StateProvince: String = "",
  Street: String = ""
) extends CaTbl

