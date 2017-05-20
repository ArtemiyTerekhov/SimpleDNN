/* Copyright 2016-present The KotlinNLP Authors. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, you can obtain one at http://mozilla.org/MPL/2.0/.
 * ------------------------------------------------------------------*/

package layers.structure.contextwindows

import com.kotlinnlp.simplednn.core.functionalities.activations.Tanh
import com.kotlinnlp.simplednn.core.arrays.AugmentedArray
import com.kotlinnlp.simplednn.core.layers.recurrent.LayerContextWindow
import com.kotlinnlp.simplednn.core.layers.recurrent.cfn.CFNLayerParameters
import com.kotlinnlp.simplednn.core.layers.recurrent.cfn.CFNLayerStructure
import com.kotlinnlp.simplednn.simplemath.NDArray

/**
 *
 */
sealed class CFNLayerContextWindow: LayerContextWindow {

  /**
   *
   */
  class Empty: CFNLayerContextWindow() {

    override fun getPrevStateLayer() = null

    override fun getNextStateLayer() = null
  }

  /**
   *
   */
  class Back: CFNLayerContextWindow() {

    override fun getPrevStateLayer(): CFNLayerStructure = buildPrevStateLayer()

    override fun getNextStateLayer() = null
  }

  /**
   *
   */
  class Front(val currentLayerOutput: NDArray): CFNLayerContextWindow() {

    override fun getPrevStateLayer() = null

    override fun getNextStateLayer(): CFNLayerStructure = buildNextStateLayer(currentLayerOutput)
  }

  /**
   *
   */
  class Bilateral(val currentLayerOutput: NDArray): CFNLayerContextWindow() {

    override fun getPrevStateLayer(): CFNLayerStructure = buildPrevStateLayer()

    override fun getNextStateLayer(): CFNLayerStructure = buildNextStateLayer(currentLayerOutput)
  }
}

/**
 *
 */
private fun buildPrevStateLayer(): CFNLayerStructure {

  val outputArray = AugmentedArray(NDArray.arrayOf(doubleArrayOf(-0.2, 0.2, -0.3, -0.9, -0.8)))
  outputArray.activate()

  return CFNLayerStructure(
    inputArray = AugmentedArray(size = 4),
    outputArray = outputArray,
    params = CFNLayerParameters(inputSize = 4, outputSize = 5),
    activationFunction = Tanh(),
    layerContextWindow = CFNLayerContextWindow.Empty()
  )
}

/**
 *
 */
private fun buildNextStateLayer(currentLayerOutput: NDArray): CFNLayerStructure {

  val outputArray = AugmentedArray(size = 5)
  outputArray.assignErrors(NDArray.arrayOf(doubleArrayOf(0.1, 0.1, -0.5, 0.7, 0.2)))

  val layer = CFNLayerStructure(
    inputArray = AugmentedArray(size = 4),
    outputArray = outputArray,
    params = CFNLayerParameters(inputSize = 4, outputSize = 5),
    activationFunction = Tanh(),
    layerContextWindow = CFNLayerContextWindow.Empty())

  layer.inputGate.assignValues(NDArray.arrayOf(doubleArrayOf(0.8, 1.0, -0.8, 0.0, 0.1)))
  layer.inputGate.assignErrors(NDArray.arrayOf(doubleArrayOf(0.7, -0.3, -0.2, 0.3, 0.6)))
  layer.forgetGate.assignErrors(NDArray.arrayOf(doubleArrayOf(0.0, 0.9, 0.2, -0.5, 1.0)))
  layer.forgetGate.assignValues(NDArray.arrayOf(doubleArrayOf(-0.2, -0.1, 0.6, -0.8, 0.5)))
  layer.activatedPrevOutput = currentLayerOutput

  return layer
}