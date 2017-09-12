package steve

import com.datasift.dropwizard.scala.ScalaApplication
import com.google.inject.Stage
import com.hubspot.dropwizard.guice.GuiceBundle
import com.typesafe.config.ConfigFactory
import io.dropwizard.setup.{Bootstrap, Environment}
import org.flywaydb.core.Flyway

object SteveServer extends ScalaApplication[SteveConfiguration] {
  val guiceBundle = GuiceBundle.newBuilder()
    .addModule(new SteveModule)
    .enableAutoConfig("controller", "steve")
    .setConfigClass(classOf[SteveConfiguration])
    .build(Stage.PRODUCTION)

  override def init(bootstrap: Bootstrap[SteveConfiguration]): Unit = {
    val flyway = new Flyway()
    val dbConfig = ConfigFactory.load().getConfig("steveDatasource.properties")
    val jdbcURL = s"jdbc:postgresql://${dbConfig.getString("serverName")}:${dbConfig.getString("portNumber")}/${dbConfig.getString("databaseName")}"
    flyway.setDataSource(jdbcURL, dbConfig.getString("user"), dbConfig.getString("password"))
    flyway.migrate()
    bootstrap.addBundle(guiceBundle)
  }

  override def run(configuration: SteveConfiguration, environment: Environment): Unit = {
    environment.jersey().setUrlPattern("/*")
  }
}
