package steve

import com.google.inject.AbstractModule
import io.dropwizard.Configuration
import store.mongo.MongoConfiguration

case class SteveConfiguration(mongoConf: MongoConfiguration) extends Configuration

class SteveModule extends AbstractModule {
  override def configure(): Unit = {
  }
}
