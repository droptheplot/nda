import org.http4s.*
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.dsl.Http4sDsl
import org.http4s.implicits.*
import repositories.*
import zio.*
import zio.interop.catz.*
import zio.interop.catz.implicits.*

import java.util.UUID

object Main extends ZIOAppDefault:
  val ref = ZLayer.fromZIO(Ref.make(Map.empty[UUID, String]))

  def run =
    (for {
      entriesRepository <- ZIO.service[EntriesRepository]
      httpRoutes = routes(entriesRepository)
      httpApp <- ZIO
        .runtime
        .flatMap { runtime =>
          BlazeServerBuilder[Task]
            .withExecutionContext(runtime.executor.asExecutionContext)
            .bindHttp(3000, "localhost")
            .withHttpApp(httpRoutes.orNotFound)
            .serve
            .compile
            .drain
        }

    } yield httpApp)
      .exitCode
      .provideLayer(ref >>> EntriesRepositoryLive.layer)

  private val dsl = Http4sDsl[Task]
  import dsl.*

  def routes(entriesRepository: EntriesRepository): HttpRoutes[Task] =
    HttpRoutes
      .of[Task] {
        case GET -> Root =>
          for {
            keys <- entriesRepository.list
            response <- Ok(keys.mkString("\n"))
          } yield response
        case req @ POST -> Root =>
          req.decode[UrlForm] { urlForm =>
            for {
              id <- ZIO.attempt(UUID.randomUUID)
              value <- ZIO
                .fromOption {
                  urlForm
                    .values
                    .get("value")
                    .flatMap(_.headOption)
                    .flatMap(str => Option.when(str.isBlank)(str))
                }
                .orElseFail(new Throwable("`value` is missing"))
              _ <- entriesRepository.create(id, value)
              response <- Ok(id.toString)
            } yield response
          }.catchAll { error =>
            Ok(error.toString)
          }
        case GET -> Root / UUIDVar(id) =>
          for {
            value <- entriesRepository.get(id)
            response <- Ok(value.toString)
          } yield response
      }
