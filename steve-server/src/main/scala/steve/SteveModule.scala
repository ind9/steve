package steve

import java.util.concurrent.Executors

import com.google.inject._
import io.dropwizard.Configuration
import slick.jdbc.JdbcBackend

import scala.concurrent.ExecutionContext

case class DataSourceConfiguration(clazz: String,
                                   serverName: String,
                                   portNumber: Int,
                                   databaseName: String,
                                   user: String,
                                   password: String,
                                   numThreads: Int)
case class SteveConfiguration(datasource:DataSourceConfiguration) extends Configuration
class SteveModule extends AbstractModule {
  lazy val executorService =  ExecutionContext.fromExecutorService(Executors.newCachedThreadPool())

  override def configure(): Unit = {
    bind(classOf[slick.jdbc.JdbcBackend.Database]).toProvider(classOf[DatabaseProvider])
    bind(classOf[ExecutionContext]).toInstance(executorService)
  }
}

@Provides
class DatabaseProvider @Inject()(configuration: SteveConfiguration) extends Provider[slick.jdbc.JdbcBackend.Database] {

  private val db = {
    println(configuration.datasource)

    slick.jdbc.JdbcBackend.Database.forConfig("steveDatasource")
  }

  override def get(): JdbcBackend.DatabaseDef = db
}