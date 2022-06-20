package repositories

import doobie.postgres.*
import doobie.postgres.implicits.*
import doobie.syntax.string.*
import doobie.implicits.*
import doobie.util.transactor.Transactor
import zio.*
import zio.interop.catz.*
import zio.interop.catz.implicits.*

import java.util.UUID
import scala.jdk.CollectionConverters.*

class PgEntriesRepository(xa: Transactor[Task]) extends EntriesRepository {
  override def create(id: UUID, value: String): Task[Unit] =
    sql"insert into entries (id, value) values ($id, $value);"
      .update
      .run
      .transact(xa)
      .map(_ => ())

  override def get(id: UUID): Task[Option[String]] =
    sql"select value from entries where id = $id limit 1;"
      .query[String]
      .option
      .transact(xa)

  override def list: Task[List[UUID]] =
    sql"select id from entries;"
      .query[UUID]
      .to[List]
      .transact(xa)
}

object PgEntriesRepository {
  val layer = ZLayer.fromFunction(new PgEntriesRepository(_))
}
