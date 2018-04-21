package org.hl7mock

import com.datastax.driver.core.querybuilder.{Insert, QueryBuilder}

import scala.collection.JavaConverters._
import scala.reflect.runtime.universe._
import com.datastax.driver.core._
import com.google.common.reflect.TypeToken
import com.eztier.cassandra.CaCommon.{camelToUnderscores, getFieldNames}
import com.eztier.cassandra._
import org.hl7mock.types.{CaPatient, _}

// Define object that extends CassandraCustomCodecImplicits.  This will implicitly be imported.
object CaPatientImplicits extends CaCustomCodecImplicits {

  // Override implicit conversion function.
  override implicit def toCaCodec[T](innerCodec: TypeCodec[UDTValue])(implicit typeTag: TypeTag[T]) = {
    typeTag.tpe match {
      case a if a == typeOf[CaPatientPhoneInfo] => CaPatientPhoneInfoCodec(innerCodec)
      case a if a == typeOf[CaPatientIdType] => CaPatientIdTypeCodec(innerCodec)
      case a if a == typeOf[CaPatientNameComponents] => CaPatientNameComponentsCodec(innerCodec)
      case a if a == typeOf[CaPatientAddress] => CaPatientAddressCodec(innerCodec)
      case a if a == typeOf[CaPatientCareTeamMember]=> CaPatientCareTeamMemberCodec(innerCodec)
      case a if a == typeOf[CaPatientEmergencyContact]=> CaPatientEmergencyContactCodec(innerCodec)
      case a if a == typeOf[CaPatientEmploymentInformation]=> CaPatientEmploymentInformationCodec(innerCodec)

      case _ => CaDefaultUdtCodec(innerCodec)
    }
  }

  val insertStatement = (keySpace: String, el: CaPatient) => {
    val t = camelToUnderscores(el.getClass.getSimpleName)
    val insert = QueryBuilder.insertInto(keySpace, t)

    insertValues(insert) values(
      camelToUnderscores("Addresses") -> el.Addresses.asJava,
      camelToUnderscores("Aliases") -> el.Aliases.asJava,
      camelToUnderscores("CareTeam") -> el.CareTeam.asJava,
      camelToUnderscores("ConfidentialName") -> el.ConfidentialName,
      camelToUnderscores("CreateDate") -> el.CreateDate,
      camelToUnderscores("DateOfBirth") -> el.DateOfBirth,
      camelToUnderscores("EmergencyContacts") -> el.EmergencyContacts.asJava,
      camelToUnderscores("EmploymentInformation") -> el.EmploymentInformation,
      camelToUnderscores("EthnicGroup") -> el.EthnicGroup,
      camelToUnderscores("HistoricalIds") -> el.HistoricalIds.asJava,
      camelToUnderscores("HomeDeployment") -> el.HomeDeployment,
      camelToUnderscores("Id") -> el.Id,
      camelToUnderscores("Ids") -> el.Ids.asJava,
      camelToUnderscores("MaritalStatus") -> el.MaritalStatus,
      camelToUnderscores("Mrn") -> el.Mrn,
      camelToUnderscores("Name") -> el.Name,
      camelToUnderscores("NameComponents") -> el.NameComponents,
      camelToUnderscores("NationalIdentifier") -> el.NationalIdentifier,
      camelToUnderscores("Race") -> el.Race.asJava,
      camelToUnderscores("Rank") -> el.Rank,
      camelToUnderscores("Sex") -> el.Sex,
      camelToUnderscores("Status") -> el.Status
    )
  }

  implicit def rowToCaPatient(row: Row) =
    CaPatient(
      Addresses = row.getList(camelToUnderscores("Addresses"), classOf[CaPatientAddress]).asScala,
      Aliases = row.getList(camelToUnderscores("Aliases"), classOf[String]).asScala,
      CareTeam = row.getList(camelToUnderscores("CareTeam"), classOf[CaPatientCareTeamMember]).asScala,
      ConfidentialName = row.getString(camelToUnderscores("ConfidentialName")),
      CreateDate = row.getTimestamp(camelToUnderscores("CreateDate")),
      DateOfBirth = row.getString(camelToUnderscores("DateOfBirth")),
      EmergencyContacts = row.getList(camelToUnderscores("EmergencyContacts"), classOf[CaPatientEmergencyContact]).asScala,
      EmploymentInformation = row.get(camelToUnderscores("EmploymentInformation"), classOf[CaPatientEmploymentInformation]),
      EthnicGroup = row.getString(camelToUnderscores("EthnicGroup")),
      HistoricalIds = row.getList(camelToUnderscores("HistoricalIds"), classOf[CaPatientIdType]).asScala,
      HomeDeployment = row.getString(camelToUnderscores("HomeDeployment")),
      Id = row.getString(camelToUnderscores("Id")),
      Ids = row.getList(camelToUnderscores("Ids"), classOf[CaPatientIdType]).asScala,
      MaritalStatus = row.getString(camelToUnderscores("MaritalStatus")),
      Mrn = row.getString(camelToUnderscores("Mrn")),
      Name = row.getString(camelToUnderscores("Name")),
      NameComponents = row.get(camelToUnderscores("NameComponents"), classOf[CaPatientNameComponents]),
      NationalIdentifier = row.getString(camelToUnderscores("NationalIdentifier")),
      Race = row.getList(camelToUnderscores("Race"), classOf[String]).asScala,
      Rank = row.getString(camelToUnderscores("Rank")),
      Sex = row.getString(camelToUnderscores("Sex")),
      Status = row.getString(camelToUnderscores("Status"))
    )

  // PreparedStatement for alpakka.
  def getPreparedStatement(keySpace: String, el: CaPatient)(implicit session: Session) = {
      val fieldNames = getFieldNames(el)

      val fields = fieldNames.map {
        camelToUnderscores(_)
      }.mkString(",")
      val placeholder = (1 to fieldNames.length).map(a => "?").mkString(",")

      val stmt = s"""insert into ${keySpace}.${camelToUnderscores(el.getClass.getSimpleName)} ($fields) values($placeholder)"""
      session.prepare(stmt)
    }

  // BoundStatement binder for alpakka.
  val getStatementBinder =
    (el: CaPatient, stmt: PreparedStatement) =>
      stmt.bind(
        el.Addresses.asJava,
        el.Aliases.asJava,
        el.CareTeam.asJava,
        el.ConfidentialName,
        el.CreateDate,
        el.DateOfBirth,
        el.EmergencyContacts.asJava,
        el.EmploymentInformation,
        el.EthnicGroup,
        el.HistoricalIds.asJava,
        el.HomeDeployment,
        el.Id,
        el.Ids.asJava,
        el.MaritalStatus,
        el.Mrn,
        el.Name,
        el.NameComponents,
        el.NationalIdentifier,
        el.Race.asJava,
        el.Rank,
        el.Sex,
        el.Status
      )

  // Extend TypeCodec and define custom encode/decode formats below:

  // CaPatientPhoneInfo
  case class CaPatientPhoneInfoCodec(innerCodec: TypeCodec[UDTValue])
    extends TypeCodec[CaPatientPhoneInfo](innerCodec.getCqlType, TypeToken.of(classOf[CaPatientPhoneInfo]))
      with CaCodec[CaPatientPhoneInfo] {

    override def toCaClass(value: UDTValue) =
      if (value == null) null
      else CaPatientPhoneInfo(Number = value.getString("number"), Type = value.getString("type"))

    override def toUDTValue(value: CaPatientPhoneInfo): UDTValue =
      if (value == null) null
      else userType.newValue.setString("number", value.Number).setString("type", value.Type)
  }

  // CaPatientIdType
  case class CaPatientIdTypeCodec(innerCodec: TypeCodec[UDTValue])
    extends TypeCodec[CaPatientIdType](innerCodec.getCqlType, TypeToken.of(classOf[CaPatientIdType]))
      with CaCodec[CaPatientIdType] {

    override def toCaClass(value: UDTValue) =
      if (value == null) null
      else CaPatientIdType(Id = value.getString("id"), Type = value.getString("type"))

    override def toUDTValue(value: CaPatientIdType): UDTValue =
      if (value == null) null
      else userType.newValue.setString("id", value.Id).setString("type", value.Type)
  }

  // CaPatientNameComponents
  case class CaPatientNameComponentsCodec(innerCodec: TypeCodec[UDTValue])
    extends TypeCodec[CaPatientNameComponents](innerCodec.getCqlType, TypeToken.of(classOf[CaPatientNameComponents]))
      with CaCodec[CaPatientNameComponents] {

    override def toCaClass(value: UDTValue) =
      if (value == null) null
      else CaPatientNameComponents(
        Academic = value.getString(camelToUnderscores("Academic")),
        FirstName = value.getString(camelToUnderscores("FirstName")),
        GivenName = value.getString(camelToUnderscores("GivenName")),
        Initials = value.getString(camelToUnderscores("Initials")),
        LastName = value.getString(camelToUnderscores("LastName")),
        LastNameFromSpouse = value.getString(camelToUnderscores("LastNameFromSpouse")),
        LastNamePrefix = value.getString(camelToUnderscores("LastNamePrefix")),
        MiddleName = value.getString(camelToUnderscores("MiddleName")),
        PreferredName = value.getString(camelToUnderscores("PreferredName")),
        PreferredNameType = value.getString(camelToUnderscores("PreferredNameType")),
        SpouseLastNameFirst = value.getString(camelToUnderscores("SpouseLastNameFirst")),
        SpouseLastNamePrefix = value.getString(camelToUnderscores("SpouseLastNamePrefix")),
        Suffix = value.getString(camelToUnderscores("Suffix")),
        Title = value.getString(camelToUnderscores("Title"))
      )

    override def toUDTValue(value: CaPatientNameComponents): UDTValue =
      if (value == null) null
      else userType.newValue
        .setString(camelToUnderscores("Academic"), value.Academic)
        .setString(camelToUnderscores("FirstName"), value.FirstName)
        .setString(camelToUnderscores("GivenName"), value.GivenName)
        .setString(camelToUnderscores("Initials"), value.Initials)
        .setString(camelToUnderscores("LastName"), value.LastName)
        .setString(camelToUnderscores("LastNameFromSpouse"), value.LastNameFromSpouse)
        .setString(camelToUnderscores("LastNamePrefix"), value.LastNamePrefix)
        .setString(camelToUnderscores("MiddleName"), value.MiddleName)
        .setString(camelToUnderscores("PreferredName"), value.PreferredName)
        .setString(camelToUnderscores("PreferredNameType"), value.PreferredNameType)
        .setString(camelToUnderscores("SpouseLastNameFirst"), value.SpouseLastNameFirst)
        .setString(camelToUnderscores("SpouseLastNamePrefix"), value.SpouseLastNamePrefix)
        .setString(camelToUnderscores("Suffix"), value.Suffix)
        .setString(camelToUnderscores("Title"), value.Title)
  }

  // CaPatientAddress
  case class CaPatientAddressCodec(innerCodec: TypeCodec[UDTValue])
    extends TypeCodec[CaPatientAddress](innerCodec.getCqlType, TypeToken.of(classOf[CaPatientAddress]))
      with CaCodec[CaPatientAddress] {

    override def toCaClass(value: UDTValue) =
      if (value == null) null
      else CaPatientAddress(
        City = value.getString(camelToUnderscores("City")),
        Country = value.getString(camelToUnderscores("Country")),
        County = value.getString(camelToUnderscores("County")),
        District = value.getString(camelToUnderscores("District")),
        Email = value.getList(camelToUnderscores("Email"), TypeToken.of(classOf[String])).asScala,
        HouseNumber = value.getString(camelToUnderscores("HouseNumber")),
        PhoneNumbers = value.getList(camelToUnderscores("PhoneNumbers"), TypeToken.of(classOf[CaPatientPhoneInfo])).asScala,
        PostalCode = value.getString(camelToUnderscores("PostalCode")),
        State = value.getString(camelToUnderscores("State")),
        Street = value.getList(camelToUnderscores("Street"), TypeToken.of(classOf[String])).asScala,
        Type = value.getString(camelToUnderscores("Type"))
      )

    override def toUDTValue(value: CaPatientAddress): UDTValue =
      if (value == null) null
      else userType.newValue
        .setString(camelToUnderscores("City"), value.City)
        .setString(camelToUnderscores("Country"), value.Country)
        .setString(camelToUnderscores("County"), value.County)
        .setString(camelToUnderscores("District"), value.District)
        .setList(camelToUnderscores("Email"), value.Email.asJava)
        .setString(camelToUnderscores("HouseNumber"), value.HouseNumber)
        .setList(camelToUnderscores("PhoneNumbers"), value.PhoneNumbers.asJava).setString(camelToUnderscores("PostalCode"), value.PostalCode)
        .setString(camelToUnderscores("State"), value.State)
        .setList(camelToUnderscores("Street"), value.Email.asJava)
        .setString(camelToUnderscores("Type"), value.Type)
  }

  // CaPatientCareTeamMember
  case class CaPatientCareTeamMemberCodec(innerCodec: TypeCodec[UDTValue])
    extends TypeCodec[CaPatientCareTeamMember](innerCodec.getCqlType, TypeToken.of(classOf[CaPatientCareTeamMember]))
      with CaCodec[CaPatientCareTeamMember] {

    override def toCaClass(value: UDTValue) =
      if (value == null) null
      else CaPatientCareTeamMember(
        Ids = value.getList(camelToUnderscores("Ids"), TypeToken.of(classOf[CaPatientIdType])).asScala,
        Name = value.getString(camelToUnderscores("Name")),
        Type = value.getString(camelToUnderscores("Type"))
      )

    override def toUDTValue(value: CaPatientCareTeamMember): UDTValue =
      if (value == null) null
      else userType.newValue
        .setList(camelToUnderscores("Ids"), value.Ids.asJava)
        .setString(camelToUnderscores("Name"), value.Name)
        .setString(camelToUnderscores("Type"), value.Type)
  }

  // CaPatientEmergencyContact
  case class CaPatientEmergencyContactCodec(innerCodec: TypeCodec[UDTValue])
    extends TypeCodec[CaPatientEmergencyContact](innerCodec.getCqlType, TypeToken.of(classOf[CaPatientEmergencyContact]))
      with CaCodec[CaPatientEmergencyContact] {

    override def toCaClass(value: UDTValue) =
      if (value == null) null
      else CaPatientEmergencyContact(
        LegalGuardian = value.getString(camelToUnderscores("LegalGuardian")),
        Name = value.getString(camelToUnderscores("Name")),
        PhoneNumbers = value.getList(camelToUnderscores("PhoneNumbers"), TypeToken.of(classOf[CaPatientPhoneInfo])).asScala,
        Relation = value.getString(camelToUnderscores("Relation"))
      )

    override def toUDTValue(value: CaPatientEmergencyContact): UDTValue =
      if (value == null) null
      else userType.newValue
        .setString(camelToUnderscores("LegalGuardian"), value.LegalGuardian)
        .setString(camelToUnderscores("Name"), value.Name)
        .setList(camelToUnderscores("PhoneNumbers"), value.PhoneNumbers.asJava)
        .setString(camelToUnderscores("Relation"), value.Relation)
  }

  // CaPatientEmploymentInformation
  case class CaPatientEmploymentInformationCodec(innerCodec: TypeCodec[UDTValue])
    extends TypeCodec[CaPatientEmploymentInformation](innerCodec.getCqlType, TypeToken.of(classOf[CaPatientEmploymentInformation]))
      with CaCodec[CaPatientEmploymentInformation] {

    override def toCaClass(value: UDTValue) =
      if (value == null) null
      else CaPatientEmploymentInformation(
        EmployerName = value.getString(camelToUnderscores("EmployerName")),
        Occupation = value.getString(camelToUnderscores("Occupation")),
        PhoneNumbers = value.getList(camelToUnderscores("PhoneNumbers"), TypeToken.of(classOf[CaPatientPhoneInfo])).asScala
      )

    override def toUDTValue(value: CaPatientEmploymentInformation): UDTValue =
      if (value == null) null
      else userType.newValue
        .setString(camelToUnderscores("EmployerName"), value.EmployerName)
        .setString(camelToUnderscores("Occupation"), value.Occupation)
        .setList(camelToUnderscores("PhoneNumbers"), value.PhoneNumbers.asJava)
  }
}
