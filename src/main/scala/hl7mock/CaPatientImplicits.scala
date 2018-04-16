package org.hl7mock

import scala.collection.JavaConverters._
import scala.reflect.runtime.universe._

import com.datastax.driver.core.{TypeCodec, UDTValue}
import com.google.common.reflect.TypeToken

import com.eztier.cassandra.CaCommon.camelToUnderscores
import com.eztier.cassandra._

import org.hl7mock.types.{CaPatientAddress, CaPatientIdType, CaPatientNameComponents, CaPatientPhoneInfo}

// Define object that extends CassandraCustomCodecImplicits.  This will implicitly be imported.
object CaPatientImplicits extends CaCustomCodecImplicits {

  // Override implicit conversion function.
  override implicit def toCaCodec[T](innerCodec: TypeCodec[UDTValue])(implicit typeTag: TypeTag[T]) = {
    typeTag.tpe match {
      case a if a == typeOf[CaPatientPhoneInfo] => CaPatientPhoneInfoCodec(innerCodec)
      case a if a == typeOf[CaPatientIdType] => CaPatientIdTypeCodec(innerCodec)
      case a if a == typeOf[CaPatientNameComponents] => CaPatientNameComponentsCodec(innerCodec)
      case a if a == typeOf[CaPatientAddress] => CaPatientAddressCodec(innerCodec)
      case _ => CaDefaultUdtCodec(innerCodec)
    }
  }

  // Extend TypeCodec and define custom encode/decode formats below.

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
        GivenNameInitials = value.getString(camelToUnderscores("GivenNameInitials")),
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
        .setString(camelToUnderscores("GivenNameInitials"), value.GivenNameInitials)
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
}
