package io.smartcat.twicas.summary

case class ParameterOptimization(models: List[ModelSummary]) {

  def getKBest(measure: (ModelSummary => Double), k: Int = 1): List[ModelSummary] = models sortBy measure take k

  def report: String = {
    models map (_.report) mkString("\n", "\n", "\n")
  }

}
