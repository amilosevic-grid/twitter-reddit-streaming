object Utils {
  def tryOrNone[T](function: () => T): Option[T] = {

      val res = function.apply()
      Option(res)

  }
}
