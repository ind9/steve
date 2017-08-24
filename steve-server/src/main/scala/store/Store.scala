package store

trait Store {
  def get[T](key:String):Option[T]
  def put[T](entity:T):String
  //def patch[T](key:String)
}


