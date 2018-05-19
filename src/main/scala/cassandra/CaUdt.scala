package com.eztier.cassandra

import java.util.Date
import scala.collection.mutable.ArrayBuffer

import com.eztier.cassandra.CaCommon.camelToUnderscores

trait CaUdt {
  private def makeSeqString(l: Seq[Any], quote: Boolean = true) = {
    "[" + l.map{
      quote match {
        case true => "'" + _.toString + "'"
        case _ => _.toString
      }
    }.mkString(",") + "]"
  }

  def handleDate(s: Date): String = {
    import org.joda.time.format.ISODateTimeFormat
    val fmt = ISODateTimeFormat.dateTime
    fmt.print(s.getTime)
  }

  def handleSeqString(l: Seq[String]): String = makeSeqString(l)
  def handleSeqDouble(l: Seq[Double]): String = makeSeqString(l, false)
  def handleSeqFloat(l: Seq[Float]): String = makeSeqString(l, false)
  def handleSeqLong(l: Seq[Long]): String = makeSeqString(l, false)
  def handleSeqShort(l: Seq[Short]): String = makeSeqString(l, false)
  def handleSeqBoolean(l: Seq[Boolean]): String = makeSeqString(l, false)
  def handleSeqDate(l: Seq[Date]): String = "[" + l.map{ "'"+handleDate(_)+"'" }.mkString(",") + "]"

  def handleCaUdt[A <: CaUdt](c: A): String = c.toCaString

  def handleSeqCaUdt[A <: CaUdt](l: Seq[A]) = "[" + l.map{ handleCaUdt(_) }.mkString(",") + "]"

  def toCaMap() = {
    val cc = this
    (Map[String, String]() /: cc.getClass.getDeclaredFields) {
      (a, f) =>
        f.setAccessible(true)
        val o = f.get(cc)

        val s: String = {
          if (f.getType.getSimpleName == "Seq")
            f.getGenericType.getTypeName match {
              case a if o.isInstanceOf[Seq[CaUdt]] => handleSeqCaUdt(o.asInstanceOf[Seq[CaUdt]])
              case a if a.contains("java.lang.String") => handleSeqString(o.asInstanceOf[Seq[String]])
              case a if a.contains("java.util.Date") => handleSeqDate(o.asInstanceOf[Seq[Date]])
              case a if a.contains("java.lang.Double") => handleSeqDouble(o.asInstanceOf[Seq[Double]])
              case a if a.contains("java.lang.Float") => handleSeqFloat(o.asInstanceOf[Seq[Float]])
              case a if a.contains("java.lang.Long") => handleSeqLong(o.asInstanceOf[Seq[Long]])
              case a if a.contains("java.lang.Short") => handleSeqShort(o.asInstanceOf[Seq[Short]])
              case a if a.contains("java.lang.Boolean") => handleSeqBoolean(o.asInstanceOf[Seq[Boolean]])
              case _ => ""
            }
          else
            o match {
              case a: CaUdt => handleCaUdt(a)
              case a: String => "'" + a + "'"
              case a: Date => "'" + handleDate(a) + "'"
              case a @ (Double | Float | Long | Short | Boolean ) => a.toString
              case _ => ""
            }
        }

        a + (camelToUnderscores(f.getName) -> s)
    }
  }

  def toCaString: String = {
    val s = toCaMap()

    val b = (ArrayBuffer[String]() /: s) {
      (a, n) =>
        a += n._1 + ":" + n._2
    }

    "{" + b.mkString(",") + "}"
  }
}

trait CaTbl extends CaUdt {
  def getInsertStmt(): String = {
    val m = toCaMap()

    val f = ((ArrayBuffer[String](), ArrayBuffer[String]()) /: m) {
      (a, n) =>
        a._1 += n._1
        a._2 += n._2
        a
    }

    s"""
       |insert into ${camelToUnderscores(getClass().getSimpleName)}
       | (${f._1.mkString(",")})
       | values
       | (${f._2.mkString(",")})
       """.stripMargin
  }
}
