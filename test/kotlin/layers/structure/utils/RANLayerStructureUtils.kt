/* Copyright 2016-present The KotlinNLP Authors. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, you can obtain one at http://mozilla.org/MPL/2.0/.
 * ------------------------------------------------------------------*/

package layers.structure.utils

import com.kotlinnlp.simplednn.core.functionalities.activations.Tanh
import com.kotlinnlp.simplednn.core.arrays.AugmentedArray
import com.kotlinnlp.simplednn.core.layers.recurrent.LayerContextWindow
import com.kotlinnlp.simplednn.core.layers.recurrent.ran.RANLayerParameters
import com.kotlinnlp.simplednn.core.layers.recurrent.ran.RANLayerStructure
import com.kotlinnlp.simplednn.simplemath.ndarray.dense.DenseNDArray
import com.kotlinnlp.simplednn.simplemath.ndarray.dense.DenseNDArrayFactory
import com.kotlinnlp.simplednn.simplemath.ndarray.Shape


/**
 *
 */
object RANLayerStructureUtils {

  /**
   *
   */
  fun buildLayer(layerContextWindow: LayerContextWindow): RANLayerStructure<DenseNDArray> = RANLayerStructure(
    inputArray = AugmentedArray(DenseNDArrayFactory.arrayOf(doubleArrayOf(-0.8, -0.9, -0.9, 1.0))),
    outputArray = AugmentedArray(DenseNDArrayFactory.emptyArray(Shape(5))),
    params = this.buildParams(),
    activationFunction = Tanh(),
    layerContextWindow = layerContextWindow)

  /**
   *
   */
  fun buildParams(): RANLayerParameters {

    val params = RANLayerParameters(inputSize = 4, outputSize = 5)

    params.inputGate.weights.values.assignValues(
      DenseNDArrayFactory.arrayOf(arrayOf(
        doubleArrayOf(0.5, 0.6, -0.8, -0.6),
        doubleArrayOf(0.7, -0.4, 0.1, -0.8),
        doubleArrayOf(0.7, -0.7, 0.3, 0.5),
        doubleArrayOf(0.8, -0.9, 0.0, -0.1),
        doubleArrayOf(0.4, 1.0, -0.7, 0.8)
      )))

    params.forgetGate.weights.values.assignValues(
      DenseNDArrayFactory.arrayOf(arrayOf(
        doubleArrayOf(0.1, 0.4, -1.0, 0.4),
        doubleArrayOf(0.7, -0.2, 0.1, 0.0),
        doubleArrayOf(0.7, 0.8, -0.5, -0.3),
        doubleArrayOf(-0.9, 0.9, -0.3, -0.3),
        doubleArrayOf(-0.7, 0.6, -0.6, -0.8)
      )))

    params.candidate.weights.values.assignValues(
      DenseNDArrayFactory.arrayOf(arrayOf(
        doubleArrayOf(-1.0, 0.2, 0.0, 0.2),
        doubleArrayOf(-0.7, 0.7, -0.3, -0.3),
        doubleArrayOf(0.3, -0.6, 0.0, 0.7),
        doubleArrayOf(-1.0, -0.6, 0.9, 0.8),
        doubleArrayOf(0.5, 0.8, -0.9, -0.8)
      )))

    params.inputGate.biases.values.assignValues(
      DenseNDArrayFactory.arrayOf(doubleArrayOf(0.4, 0.0, -0.3, 0.8, -0.4))
    )

    params.forgetGate.biases.values.assignValues(
      DenseNDArrayFactory.arrayOf(doubleArrayOf(0.9, 0.2, -0.9, 0.2, -0.9))
    )

    params.candidate.biases.values.assignValues(
      DenseNDArrayFactory.arrayOf(doubleArrayOf(0.2, 0.0, -0.9, 0.7, -0.3))
    )

    params.inputGate.recurrentWeights.values.assignValues(
      DenseNDArrayFactory.arrayOf(arrayOf(
        doubleArrayOf(0.0, 0.8, 0.8, -1.0, -0.7),
        doubleArrayOf(-0.7, -0.8, 0.2, -0.7, 0.7),
        doubleArrayOf(-0.9, 0.9, 0.7, -0.5, 0.5),
        doubleArrayOf(0.0, -0.1, 0.5, -0.2, -0.8),
        doubleArrayOf(-0.6, 0.6, 0.8, -0.1, -0.3)
      )))

    params.forgetGate.recurrentWeights.values.assignValues(
      DenseNDArrayFactory.arrayOf(arrayOf(
        doubleArrayOf(0.1, -0.6, -1.0, -0.1, -0.4),
        doubleArrayOf(0.5, -0.9, 0.0, 0.8, 0.3),
        doubleArrayOf(-0.3, -0.9, 0.3, 1.0, -0.2),
        doubleArrayOf(0.7, 0.2, 0.3, -0.4, -0.6),
        doubleArrayOf(-0.2, 0.5, -0.2, -0.9, 0.4)
      )))

    return params
  }

  /**
   *
   */
  fun getOutputGold() = DenseNDArrayFactory.arrayOf(doubleArrayOf(0.57, 0.75, -0.15, 1.64, 0.45))
}
