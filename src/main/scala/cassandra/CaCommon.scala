package com.eztier.cassandra

import java.util.Date

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

}
