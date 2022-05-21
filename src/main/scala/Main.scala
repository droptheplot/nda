import org.http4s.*
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.dsl.Http4sDsl
import org.http4s.implicits.*
import zio.*
import zio.interop.catz.*
import zio.interop.catz.implicits.*

object Main extends ZIOAppDefault:
  def run =
    ZIO
      .runtime
      .flatMap { implicit runtime =>
        BlazeServerBuilder[Task]
          .withExecutionContext(runtime.executor.asExecutionContext)
          .bindHttp(3000, "localhost")
          .withHttpApp(routes.orNotFound)
          .serve
          .compile
          .drain
      }
      .exitCode

  private val dsl = Http4sDsl[Task]
  import dsl.*

  def routes: HttpRoutes[Task] =
    HttpRoutes
      .of[Task] {
        case GET -> Root =>
          Ok("Hello")
      }
