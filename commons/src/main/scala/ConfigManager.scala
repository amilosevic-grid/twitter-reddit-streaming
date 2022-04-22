import pureconfig._
import pureconfig.generic.auto._

object ConfigManager {
  val configPath = "commons/src/main/resources/application.conf"
  def load: TwitterConfigurations =
    ConfigSource.file(configPath).load[TwitterConfigurations] match {
      case Left(error) => throw new RuntimeException(error.prettyPrint())
      case Right(result) => result
    }
}