package repositories

import zio.*
import zio._

import java.util.UUID

trait EntriesRepository {
  def create(id: UUID, value: String): Task[Unit]
  def get(id: UUID): Task[Option[String]]
  def list: Task[List[UUID]]
}

class EntriesRepositoryLive(ref: Ref[Map[UUID, String]]) extends EntriesRepository {
  override def create(id: UUID, value: String): Task[Unit] = ref.update(_ + (id -> value))

  override def get(id: UUID): Task[Option[String]] = ref.get.map(_.get(id))

  override def list: Task[List[UUID]] = ref.get.map(_.keys.toList)
}

object EntriesRepositoryLive {
  val layer = ZLayer.fromFunction(new EntriesRepositoryLive(_))
}
