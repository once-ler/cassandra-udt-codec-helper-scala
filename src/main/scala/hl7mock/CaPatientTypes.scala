package org.hl7mock.types

import java.util.Date

import com.eztier.cassandra.{CaTbl, CaUdt}

case class CaPatientPhoneInfo (
  Number: String = "",
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
  Email: Seq[String] = Seq(),
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
  DateOfBirth: String = "",
  EmergencyContacts: Seq[CaPatientEmergencyContact] = Seq(),
  EmploymentInformation: CaPatientEmploymentInformation = CaPatientEmploymentInformation(),
  EthnicGroup: String = "",
  HistoricalIds: Seq[CaPatientIdType] = Seq(),
  HomeDeployment: String = "",
  Id: String = "",
  Ids: Seq[CaPatientIdType] = Seq(),
  MaritalStatus: String = "",
  Mrn: String = "",
  Name: String = "",
  NameComponents: CaPatientNameComponents = CaPatientNameComponents(),
  NationalIdentifier: String = "",
  Race: Seq[String] = Seq(),
  Rank: String = "",
  Sex: String = "",
  Status: String = ""
) extends CaTbl
