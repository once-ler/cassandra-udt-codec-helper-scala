package com.eztier.cassandra

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.alpakka.cassandra.scaladsl.{CassandraFlow, CassandraSink, CassandraSource}
import com.datastax.driver.core.{BoundStatement, PreparedStatement, Session, SimpleStatement}

trait CaStreamFlowTask {
  implicit val session: Session

  implicit val system = ActorSystem()
  implicit val ec = system.dispatcher
  implicit val mat = ActorMaterializer()

  def getSourceStream(cqlStmt: String, fetchSize: Int = 20) = {
    val stmt = new SimpleStatement(cqlStmt).setFetchSize(fetchSize)
    CassandraSource(stmt)
  }

  def getInsertFlow[T](preparedStatement: PreparedStatement, statementBinder: (T, PreparedStatement) => BoundStatement) = {
    CassandraFlow.createWithPassThrough(parallelism = 2, preparedStatement, statementBinder)
  }

  def getInsertSink[T](preparedStatement: PreparedStatement, statementBinder: (T, PreparedStatement) => BoundStatement) = {
    CassandraSink(parallelism = 2, preparedStatement, statementBinder)
  }
}
