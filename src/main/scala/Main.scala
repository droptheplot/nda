import org.http4s.{DefaultCharset, *}
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.`Content-Type`
import org.http4s.implicits.*
import play.twirl.api.{Content, Html, JavaScript, Txt, Xml}
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
            entities <- entriesRepository.list
            response <- Ok(views.html.index(entities, None))
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
                    .flatMap(str => Option.when(!str.isBlank)(str))
                }
                .orElseFail(new Throwable("`value` is missing"))
              _ <- entriesRepository.create(id, value)
              response <- Ok(views.html.redirect(s"/$id"))
            } yield response
          }.catchAll { error =>
            for {
              entities <- entriesRepository.list
              response <- Ok(views.html.index(entities, Some(error.getMessage)))
            } yield response
          }
        case GET -> Root / UUIDVar(id) =>
          for {
            valueOpt <- entriesRepository.get(id)
            response <- valueOpt
              .map(value => Ok(views.html.entity(id, value)))
              .getOrElse(Ok(views.html.notFound()))
          } yield response
      }

  implicit def htmlContentEncoder(implicit charset: Charset = DefaultCharset): EntityEncoder[Task, Html] =
    contentEncoder(MediaType.text.html)

  private def contentEncoder[C <: Content](mediaType: MediaType)(implicit charset: Charset): EntityEncoder[Task, C] =
    EntityEncoder
      .stringEncoder
      .contramap[C](content => content.body)
      .withContentType(`Content-Type`(mediaType, charset))
