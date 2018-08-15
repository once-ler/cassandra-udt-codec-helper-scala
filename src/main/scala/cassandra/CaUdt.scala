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
  def handleSeqBigDecimal(l: Seq[BigDecimal]): String = makeSeqString(l, false)
  def handleSeqDouble(l: Seq[Double]): String = makeSeqString(l, false)
  def handleSeqFloat(l: Seq[Float]): String = makeSeqString(l, false)
  def handleSeqLong(l: Seq[Long]): String = makeSeqString(l, false)
  def handleSeqInt(l: Seq[Int]): String = makeSeqString(l, false)
  def handleSeqShort(l: Seq[Short]): String = makeSeqString(l, false)
  def handleSeqByte(l: Seq[Byte]): String = makeSeqString(l, false)
  def handleSeqBoolean(l: Seq[Boolean]): String = makeSeqString(l, false)
  def handleSeqDate(l: Seq[Date]): String = "[" + l.map{ "'"+handleDate(_)+"'" }.mkString(",") + "]"

  def handleCaUdt[A <: CaUdt](c: A): String = c.toCaString

  def handleSeqCaUdt[A <: CaUdt](l: Seq[A]) = "[" + l.map{ handleCaUdt(_) }.mkString(",") + "]"

  private def pat = "(boolean|byte|short|int|long|float|double)".r

  def toCaMap() = {
    val cc = this
    (Map[String, String]() /: cc.getClass.getDeclaredFields) {
      (a, f) =>
        f.setAccessible(true)
        val o = f.get(cc)

        val s: String = {
          // o is AnyRef, Double | Float | Long | Int | Short | Byte | Boolean auto unboxed
          o match {
            case a: CaUdt => handleCaUdt(a)
            case a: String => "'" + a + "'"
            case a: Date => "'" + handleDate(a) + "'"
            case a: java.math.BigDecimal => a.toString
            case a if pat.findFirstIn(f.getType.getSimpleName) != None => a.toString
            case a: Seq[CaUdt] => handleSeqCaUdt(o.asInstanceOf[Seq[CaUdt]])
            case a: Seq[String] => handleSeqString(o.asInstanceOf[Seq[String]])
            case a: Seq[Date] => handleSeqDate(o.asInstanceOf[Seq[Date]])
            case a: Seq[BigDecimal] => handleSeqBigDecimal(o.asInstanceOf[Seq[BigDecimal]])
            case a: Seq[Double] => handleSeqDouble(o.asInstanceOf[Seq[Double]])
            case a: Seq[Float] => handleSeqFloat(o.asInstanceOf[Seq[Float]])
            case a: Seq[Long] => handleSeqLong(o.asInstanceOf[Seq[Long]])
            case a: Seq[Int] => handleSeqInt(o.asInstanceOf[Seq[Int]])
            case a: Seq[Short] => handleSeqShort(o.asInstanceOf[Seq[Short]])
            case a: Seq[Byte] => handleSeqByte(o.asInstanceOf[Seq[Byte]])
            case _ => ""
          }
        }

        a + (camelToUnderscores(f.getName) -> s)
    }
  }

  def toCaType() = {
    val cc = this
    (Map[String, String]() /: cc.getClass.getDeclaredFields) {
      (a, f) =>
        f.setAccessible(true)
        val o = f.get(cc)

        val s: String = {
          o match {
            case a: CaUdt => "frozen<" + camelToUnderscores(f.getName) + ">"
            case a: String => "text"
            case a: Date => "timestamp"
            case a: java.math.BigDecimal => "decimal"
            case a: java.lang.Float => "float"
            case a: java.lang.Double => "double"
            case a: java.lang.Long => "bigint"
            case a: java.lang.Integer => "int"
            case a: java.lang.Short => "smallint"
            case a: java.lang.Byte => "tinyint"
            case a: java.lang.Boolean => "boolean"
            case a: Seq[CaUdt] => "list<frozen<" + camelToUnderscores(f.getName) + ">>"
            case a: Seq[String] => "list<text>"
            case a: Seq[Date] => "list<timestamp>"
            case a: Seq[BigDecimal] => "list<decimal>"
            case a: Seq[Double] => "list<double>"
            case a: Seq[Float] => "list<float>"
            case a: Seq[Long] => "list<bigint>"
            case a: Seq[Int] => "list<int>"
            case a: Seq[Short] => "list<smallint>"
            case a: Seq[Byte] => "list<tinyint>"
            case a: Seq[Boolean] => "list<boolean>"
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

  def getCreateStmt(objectType: String = "type") = {
    val m = toCaType()

    val f = (ArrayBuffer[String]() /: m) {
      (a, n) =>
      a += n._1 + " " + n._2
      a
    }

    s"""create ${objectType} if not exists ${camelToUnderscores(getClass().getSimpleName)} (
       |${f.mkString(",")}
       |);
     """.stripLineEnd.stripMargin
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

    s"""insert into ${camelToUnderscores(getClass().getSimpleName)} (${f._1.mkString(",")}) values (${f._2.mkString(",")})""".stripMargin
  }

  def getCreateStmt(partitionKeys: String*) = (clusteringKeys: Seq[String]) => (orderBy: Option[String], direction: Option[Int]) => {

    val f = getClass.getDeclaredFields.map(_.getName)

    val pk = partitionKeys.collect{
      case a if f.find(_ == a) != None => camelToUnderscores(a)
    }.mkString(",")

    val ck = clusteringKeys.collect{
      case a if f.find(_ == a) != None => camelToUnderscores(a)
    }.mkString(",")

    val sb = orderBy match {
      case Some(a) if a.length > 0 && f.find(_ == a) != None =>
        camelToUnderscores(a) + (
          direction match {
          case Some(b) => if (b > 0) "asc" else "desc"
          case None => "asc"
        })
      case None => ""
    }

    val t = super.getCreateStmt("table")

    t.substring(0, t.length - 2) +
      s"((${pk})" +
      (if (ck.length > 0) s",${ck})" else ")") +
      sb
  }
}
