package io.smartcat.twicas.summary

class ParameterOptimization(models:List[ModelSummary]) {

  def getKBest(measure:(ModelSummary => Double), k:Int = 1):List[ModelSummary] = models sortBy measure  take k


}
