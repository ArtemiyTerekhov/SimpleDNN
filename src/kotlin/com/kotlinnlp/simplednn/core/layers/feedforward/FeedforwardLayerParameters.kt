/* Copyright 2016-present The KotlinNLP Authors. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, you can obtain one at http://mozilla.org/MPL/2.0/.
 * ------------------------------------------------------------------*/

package com.kotlinnlp.simplednn.core.layers.feedforward

import com.kotlinnlp.simplednn.core.layers.LayerParameters
import com.kotlinnlp.simplednn.core.functionalities.randomgenerators.RandomGenerator
import com.kotlinnlp.simplednn.core.layers.ParametersUnit

/**
 * The parameters of the layer of type Feedforward.
 *
 * @property inputSize input size
 * @property outputSize output size
 * @property sparseInput whether the weights connected to the input are sparse or not
 */
class FeedforwardLayerParameters(
  inputSize: Int,
  outputSize: Int,
  private val sparseInput: Boolean = false
) : LayerParameters(inputSize = inputSize, outputSize = outputSize) {

  /**
   *
   */
  val unit = ParametersUnit(inputSize = this.inputSize, outputSize = this.outputSize, sparseInput = this.sparseInput)

  /**
   *
   */
  init {
    this.paramsList = arrayListOf(
      this.unit.weights,
      this.unit.biases
    )
  }

  /**
   *
   */
  override fun initialize(randomGenerator: RandomGenerator, biasesInitValue: Double): Unit {
    require(!this.sparseInput) { "Cannot randomize sparse weights" }

    this.unit.weights.values.randomize(randomGenerator)
    this.unit.biases.values.assignValues(biasesInitValue)
  }
}
