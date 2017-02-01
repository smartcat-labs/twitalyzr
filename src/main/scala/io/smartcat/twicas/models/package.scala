package io.smartcat.twicas

package object models {

  def generator[T](x: List[List[T]]): List[List[T]] = x match {
    case Nil => List(Nil)
    case h :: _ => h.flatMap(i => generator(x.tail).map(i :: _))
  }

}
