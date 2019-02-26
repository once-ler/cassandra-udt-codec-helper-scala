package com.eztier.cassandra

import java.util.Date

import scala.collection.mutable.ArrayBuffer
import scala.reflect.runtime.universe._

object CaCommon {
  def getFieldValues(in: AnyRef,
                     filterFunc: java.lang.reflect.Field => Boolean = (_) => true,
                     formatFunc: Any => Any = a => a) = {
    (List[Any]() /: in.getClass.getDeclaredFields.filter(filterFunc)) {
      (a, f) =>
        f.setAccessible(true)
        a :+ formatFunc(f.get(in))
    }
  }

  def getFieldNames(in: AnyRef,
                    filterFunc: java.lang.reflect.Field => Boolean = (_) => true,
                    formatFunc: String => String = a => a) = {
    (List[String]() /: in.getClass.getDeclaredFields.filter(filterFunc)) {
      (a, f) =>
        a :+ formatFunc(f.getName)
    }
  }

  def camelToUnderscores(name: String) = "[A-Z\\d]".r.replaceAllIn(name.charAt(0).toLower.toString + name.substring(1), {m =>
    "_" + m.group(0).toLowerCase()
  })

  def underscoreToCamel(name: String) = "_([a-z\\d])".r.replaceAllIn(name, {m =>
    m.group(1).toUpperCase()
  })

  def camelToSpaces(name: String) = "[A-Z\\d]".r.replaceAllIn(name, (m => " " + m.group(0)))

  private def classAccessors[T: TypeTag]: List[MethodSymbol] = typeOf[T].members.collect {
    case m: MethodSymbol if m.isCaseAccessor => m
  }.toList

  def toCaTypeTest[T](implicit typeTag: TypeTag[T]) = {
    val members = classAccessors[T]

    members
  }

  def toCaType[T](implicit typeTag: TypeTag[T]) = {
    val members = classAccessors[T]

    val m = (Map[String, String]() /: members) {
      (a, f) =>
        val o = f.info.resultType
        val n = f.name.toString
        val v = o.typeArgs

        val s: String = {
          o match {
            case a if a <:< typeOf[CaUdt] => "frozen<" + camelToUnderscores(o.typeSymbol.name.toString) + ">"
            case a if a =:= typeOf[String] => "text"
            case a if a =:= typeOf[Date] => "timestamp"
            case a if a =:= typeOf[java.math.BigDecimal] => "decimal"
            case a if a =:= typeOf[Float] => "float"
            case a if a =:= typeOf[Double] => "double"
            case a if a =:= typeOf[Long] => "bigint"
            case a if a =:= typeOf[Int] => "int"
            case a if a =:= typeOf[Short] => "smallint"
            case a if a =:= typeOf[Byte] => "tinyint"
            case a if a =:= typeOf[Boolean] => "boolean"
            case a if a <:< typeOf[Seq[CaUdt]] => "list<frozen<" + camelToUnderscores(v(0).typeSymbol.name.toString) + ">>"
            case a if a =:= typeOf[Seq[String]] => "list<text>"
            case a if a =:= typeOf[Seq[Date]] => "list<timestamp>"
            case a if a =:= typeOf[Seq[BigDecimal]] => "list<decimal>"
            case a if a =:= typeOf[Seq[Double]] => "list<double>"
            case a if a =:= typeOf[Seq[Float]] => "list<float>"
            case a if a =:= typeOf[Seq[Long]] => "list<bigint>"
            case a if a =:= typeOf[Seq[Int]] => "list<int>"
            case a if a =:= typeOf[Seq[Short]] => "list<smallint>"
            case a if a =:= typeOf[Seq[Byte]] => "list<tinyint>"
            case a if a =:= typeOf[Seq[Boolean]] => "list<boolean>"
            case _ => ""
          }
        }

        a + (camelToUnderscores(n) -> s)
    }

    m
  }

  def getCreateStmt[T](implicit typeTag: TypeTag[T]): Seq[String] = {
    val o = typeTag.tpe.resultType

    val objectType = o match {
      case a if a <:< typeOf[CaTbl] => "table"
      case _ => "type"
    }

    val m = toCaType[T]

    val f = (ArrayBuffer[String]() /: m) {
      (a, n) =>
        a += n._1 + " " + n._2
        a
    }

    Seq(s"create ${objectType} if not exists ${camelToUnderscores(o.typeSymbol.name.toString)} (${f.mkString(",")});")
  }

  def getCreateStmt[T: TypeTag](partitionKeys: String*)(clusteringKeys: String*)(orderBy: Option[String] = None, direction: Option[Int] = None): Seq[String] = {

    val t: Seq[String] = getCreateStmt[T]

    val o = typeTag.tpe.resultType
    val n = camelToUnderscores(o.typeSymbol.name.toString)

    o match {
      case a if a <:< typeOf[CaTbl] =>
        val members = classAccessors[T]
        val f = members.map(_.name.toString)

        val pk = partitionKeys.collect {
          case a if f.find(_ == a) != None => camelToUnderscores(a)
        }.mkString(",")

        val ck = clusteringKeys.collect {
          case a if f.find(_ == a) != None => camelToUnderscores(a)
        }.mkString(",")

        val sb = orderBy match {
          case Some(a) if a.length > 0 && f.find(_ == a) != None =>
            val sort = direction match {
              case Some(b) => if (b > 0) "asc" else "desc"
              case None => "asc"
            }

            s" with clustering order by (${camelToUnderscores(a)} ${sort})"
          case None => ""
        }

        val t0 = t(0)
        val trim = t0.substring(0, t0.length - 2)

        {
          trim +
            s", primary key ((${pk})" +
            (if (ck.length > 0) s", ${ck}))" else "))") +
            sb + ";\n"
        }.split('\n')

      case _ => t // Just return type creation script b/c there's no primary keys.
    }

  }
}
