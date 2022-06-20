case class Config(host: String, port: Int, database: Config.Database)

object Config {
  case class Database(url: String, user: String, password: String)
}
