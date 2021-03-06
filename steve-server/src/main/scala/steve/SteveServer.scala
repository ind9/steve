package steve

import com.datasift.dropwizard.scala.ScalaApplication
import com.google.inject.Stage
import com.hubspot.dropwizard.guice.GuiceBundle
import com.typesafe.config.ConfigFactory
import health.SteveHealthCheck
import io.dropwizard.configuration.{EnvironmentVariableSubstitutor, SubstitutingSourceProvider}
import io.dropwizard.setup.{Bootstrap, Environment}
import org.flywaydb.core.Flyway

object SteveServer extends ScalaApplication[SteveConfiguration] {
  val guiceBundle = GuiceBundle.newBuilder()
    .addModule(new SteveModule)
    .enableAutoConfig("controller", "steve")
    .setConfigClass(classOf[SteveConfiguration])
    .build(Stage.PRODUCTION)

  override def init(bootstrap: Bootstrap[SteveConfiguration]): Unit = {
    bootstrap.setConfigurationSourceProvider(
      new SubstitutingSourceProvider(
        bootstrap.getConfigurationSourceProvider,
        new EnvironmentVariableSubstitutor(false))
    )
    val flyway = new Flyway()
    val dbConfig = ConfigFactory.load().getConfig("steveDatasource.properties").resolve()
    val jdbcURL = s"jdbc:postgresql://${dbConfig.getString("serverName")}:${dbConfig.getInt("portNumber").toString}/${dbConfig.getString("databaseName")}"
    flyway.setDataSource(jdbcURL, dbConfig.getString("user"), dbConfig.getString("password"))
    flyway.migrate()
    bootstrap.addBundle(guiceBundle)
  }

  override def run(configuration: SteveConfiguration, environment: Environment): Unit = {
    environment.healthChecks().register("steve", new SteveHealthCheck)
    environment.jersey().setUrlPattern("/*")
  }
}
