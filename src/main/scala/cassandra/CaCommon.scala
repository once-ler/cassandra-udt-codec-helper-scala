package com.eztier.cassandra

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
}
