package io.smartcat.twicas.summary

case class ParameterOptimization(models: List[ModelSummary]) {

  def getKBest(measure: (ModelSummary => Double), k: Int = 1): List[ModelSummary] = models sortBy measure take k

  override def toString: String = {
    models map (_.toString) mkString("\n", "\n", "\n")
  }

}
