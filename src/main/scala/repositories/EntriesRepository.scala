package repositories

import zio.*

import java.util.UUID

trait EntriesRepository {
  def create(id: UUID, value: String): Task[Unit]
  def get(id: UUID): Task[Option[String]]
  def list: Task[List[UUID]]
}
