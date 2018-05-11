package com.eztier.hl7mock

import ca.uhn.hl7v2.{DefaultHapiContext, HL7Exception}
import ca.uhn.hl7v2.model.Message
import ca.uhn.hl7v2.parser.{CanonicalModelClassFactory, EncodingNotSupportedException}
import ca.uhn.hl7v2.validation.impl.NoValidation

object Hapi {
  private val pipeParser = {
    val hapiContext = new DefaultHapiContext()
    hapiContext.setModelClassFactory(new CanonicalModelClassFactory("2.3.1"))
    hapiContext.setValidationContext(new NoValidation)
    hapiContext.getPipeParser()
  }

  def parseMessage(in: String): Option[Message] =
    try {
      val a = pipeParser.parse(in)
      Some(a)
    } catch {
      case e: EncodingNotSupportedException => { e.printStackTrace(); None }
      case e1: HL7Exception => { e1.printStackTrace(); None }
    }
}
