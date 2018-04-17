package com.eztier.cassandra

object CaCommon {
  def getFieldNames(cc: AnyRef) =
    (Seq[String]() /: cc.getClass.getDeclaredFields) {
      (a, f) =>
        f.setAccessible(true)
        a :+ f.getName
    }

  def camelToUnderscores(name: String) = "[A-Z\\d]".r.replaceAllIn(name.charAt(0).toLower.toString + name.substring(1), {m =>
    "_" + m.group(0).toLowerCase()
  })

  def underscoreToCamel(name: String) = "_([a-z\\d])".r.replaceAllIn(name, {m =>
    m.group(1).toUpperCase()
  })
}
