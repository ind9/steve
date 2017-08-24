package store.mongo

import com.google.inject.Inject
import org.mongodb.scala.connection.ClusterSettings
import org.mongodb.scala.{MongoClientSettings, ServerAddress}
import steve.SteveConfiguration

import scala.collection.JavaConverters._

case class ServerPort(host:String, port:Int)

case class MongoConfiguration(hosts: List[ServerPort]){
  def getServerAddresses = hosts.map(host => ServerAddress(host.host,host.port))

}

class MongoConnection @Inject()(configuration: SteveConfiguration) {

  val clusterSettings = ClusterSettings.builder().hosts(configuration.mongoConf.getServerAddresses.asJava)
  val mongoClient = MongoClientSettings.builder().

}
