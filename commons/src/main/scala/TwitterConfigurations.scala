case class TwitterConfigurations(consumerKey: String,
                                 consumerSecret: String,
                                 accessToken: String,
                                 tokenSecret: String,
                                 bearerToken: String)

object TwitterConfigurations {
  val HASHTAG: String = "bigdata"
}