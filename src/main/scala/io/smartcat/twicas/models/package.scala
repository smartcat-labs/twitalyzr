package io.smartcat.twicas

package object models {

  def generator(x: List[List[Double]]): List[List[Double]] = x match {
    case Nil => List(Nil)
    case h :: _ => h.flatMap(i => generator(x.tail).map(i :: _))
  }

}
