package com.eztier.hl7mock

import java.util.Date

import ca.uhn.hl7v2.model.Message
import ca.uhn.hl7v2.model.v231.datatype.{CX, XPN}
import ca.uhn.hl7v2.model.v231.segment.PID
import com.eztier.hl7mock.types._
import com.eztier.hl7mock.HapiToCaHl7Implicits._

object HapiToCaPatientImplicits {
  implicit def fromMessageToCaPatient(in: Message): CaPatient = {
    val header: CaHl7 = in
    val pid = in.get("PID").asInstanceOf[PID]

    CaPatient(
      CreateDate = header.CreateDate,
      Ethnicity = pid toEthnicGroup,
      Race = pid toRace,
      Addresses = pid toCaPatientAddress,
      Ids = pid toCaPatientIdType,
      Id = pid toMrn,
      Mrn = pid toMrn,
      NameComponents = pid toCaPatientNameComponents,
      Name = pid toFullName,
      Gender = pid toGender,
      DateOfBirth = pid toDateOfBirth
    )
  }

  implicit def fromXPNToFullName(in: XPN): String = {
    val mn = in.getMiddleInitialOrName.getValueOrEmpty
    s"${in.getFamilyLastName.getFamilyName.getValueOrEmpty}, ${in.getGivenName.getValueOrEmpty}${if (mn.length > 0) " " + mn else ""}"
  }

  implicit class WrapPID(in: PID) {
    def toEthnicGroup(): Seq[String] = in.getEthnicGroup.map(a => a.getIdentifier.getValueOrEmpty)

    def toRace(): Seq[String] = in.getRace.map(a => a.getIdentifier.getValueOrEmpty)

    def toMrn(): String = in.getPatientIdentifierList.headOption.getOrElse(new CX(in.getMessage)).getID.getValueOrEmpty

    def toFullName(): String = fromXPNToFullName(in.getPatientName.headOption.getOrElse(new XPN(in.getMessage)))

    def toGender(): String = in.getSex.getValueOrEmpty

    def toDateOfBirth(): Date = in.getDateTimeOfBirth.getTimeOfAnEvent.getValueAsDate

    def toCaPatientIdType(): Seq[CaPatientIdType] = in.getPatientIdentifierList.map {
      a =>
        CaPatientIdType(
          Id = a.getID.getValueOrEmpty,
          Type = a.getIdentifierTypeCode.getValueOrEmpty
        )
      }

    def toCaPatientNameComponents: Seq[CaPatientNameComponents] = {
      in.getPatientName.map {
        a =>
          CaPatientNameComponents(
            FirstName = a.getGivenName.getValueOrEmpty,
            MiddleName = a.getMiddleInitialOrName.getValueOrEmpty,
            LastName = a.getFamilyLastName.getFamilyName.getValueOrEmpty,
            LastNamePrefix = a.getFamilyLastName.getLastNamePrefix.getValueOrEmpty,
            Academic = a.getDegreeEgMD.getValueOrEmpty,
            Suffix = a.getSuffixEgJRorIII.getValueOrEmpty,
            Title = a.getPrefixEgDR.getValueOrEmpty
          )
        }
      }

    def toCaPatientAddress: Seq[CaPatientAddress] = {
      val ph = in.getPhoneNumberHome.map {
        a =>
          CaPatientPhoneInfo(
            Number = "(" + a.getAreaCityCode.getValue + ") " + a.getPhoneNumber.getValue,
            Type = "Home"
          )
      }

      val pb = in.getPhoneNumberBusiness.map {
        a =>
          CaPatientPhoneInfo(
            Number = "(" + a.getAreaCityCode.getValue + ") " + a.getPhoneNumber.getValue,
            Type = "Business"
          )
      }

      val eh = in.getPhoneNumberHome.map {
        a =>
          CaPatientEmailInfo(
            Email = a.getEmailAddress.getValueOrEmpty,
            Type = "Home"
          )
      }

      val eb = in.getPhoneNumberBusiness.map {
        a =>
          CaPatientEmailInfo(
            Email = a.getEmailAddress.getValueOrEmpty,
            Type = "Business"
          )
      }

      in.getPatientAddress.map {
        a =>
          CaPatientAddress(
            City = a.getCity.getValueOrEmpty,
            Country = a.getCountry.getValueOrEmpty,
            County = a.getCountyParishCode.getValueOrEmpty,
            District = a.getCensusTract.getValueOrEmpty,
            Email = eh ++ eb,
            HouseNumber = "",
            PhoneNumbers = ph ++ pb,
            PostalCode = a.getZipOrPostalCode.getValueOrEmpty,
            State = a.getStateOrProvince.getValueOrEmpty,
            Street = Seq[String](a.getStreetAddress.getValueOrEmpty),
            Type = a.getAddressType.getValueOrEmpty
          )
        }
      }

  }
}
