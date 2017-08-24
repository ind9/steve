package steve

import com.datasift.dropwizard.scala.ScalaApplication
import com.google.inject.Stage
import com.hubspot.dropwizard.guice.GuiceBundle
import io.dropwizard.setup.{Bootstrap, Environment}

object SteveServer extends ScalaApplication[SteveConfiguration]{
  val guiceBundle = GuiceBundle.newBuilder()
    .addModule(new SteveModule)
    .enableAutoConfig("controller","steve")
    .setConfigClass(classOf[SteveConfiguration])
    .build(Stage.PRODUCTION)

  override def init(bootstrap: Bootstrap[SteveConfiguration]): Unit = {
    bootstrap.addBundle(guiceBundle)
  }
  override def run(configuration: SteveConfiguration, environment: Environment): Unit = {
    environment.jersey().setUrlPattern("/*")
  }
}
