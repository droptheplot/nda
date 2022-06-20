package repositories

import zio.*

import java.util.UUID

class RefEntriesRepository(ref: Ref[Map[UUID, String]]) extends EntriesRepository {
  override def create(id: UUID, value: String): Task[Unit] = ref.update(_ + (id -> value))

  override def get(id: UUID): Task[Option[String]] = ref.get.map(_.get(id))

  override def list: Task[List[UUID]] = ref.get.map(_.keys.toList)
}

object RefEntriesRepository {
  val layer = ZLayer.fromFunction(new RefEntriesRepository(_))
}
