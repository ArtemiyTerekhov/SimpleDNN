/* Copyright 2016-present The KotlinNLP Authors. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, you can obtain one at http://mozilla.org/MPL/2.0/.
 * ------------------------------------------------------------------*/

package com.kotlinnlp.simplednn.core.neuralprocessor.feedforward

import com.kotlinnlp.simplednn.core.arrays.DistributionArray
import com.kotlinnlp.simplednn.core.neuralnetwork.NetworkParameters
import com.kotlinnlp.simplednn.core.neuralnetwork.NeuralNetwork
import com.kotlinnlp.simplednn.core.neuralnetwork.structure.feedforward.FeedforwardNetworkStructure
import com.kotlinnlp.simplednn.core.neuralprocessor.NeuralProcessor
import com.kotlinnlp.simplednn.simplemath.ndarray.dense.DenseNDArray
import com.kotlinnlp.simplednn.simplemath.ndarray.NDArray

/**
 * The [FeedforwardNeuralProcessor] acts on the [neuralNetwork] performing predictions
 * and training based on Examples.
 *
 * @property neuralNetwork a [NeuralNetwork]
 */
class FeedforwardNeuralProcessor<InputNDArrayType : NDArray<InputNDArrayType>>(
  neuralNetwork: NeuralNetwork
) : NeuralProcessor(neuralNetwork) {

  /**
   * The structure as support of forward and backward.
   */
  var structure = FeedforwardNetworkStructure<InputNDArrayType>(
    layersConfiguration = this.neuralNetwork.layersConfiguration,
    params = this.neuralNetwork.model)

  /**
   * The contributes of the model parameters to forward the input to the output
   */
  private val forwardParamsContributes: NetworkParameters = this.neuralNetwork.parametersErrorsFactory()

  /**
   * The errors of the network model parameters
   */
  private val backwardParamsErrors: NetworkParameters = this.neuralNetwork.parametersErrorsFactory()

  /**
   *
   * @return
   */
  override fun getOutput(copy: Boolean): DenseNDArray {
    return if (copy) {
      this.structure.outputLayer.outputArray.values.copy()
    } else {
      this.structure.outputLayer.outputArray.values
    }
  }

  /**
   *
   */
  override fun getParamsErrors(copy: Boolean): NetworkParameters {

    val paramsError: NetworkParameters

    if (copy) {
      paramsError = this.neuralNetwork.parametersErrorsFactory()
      paramsError.assignValues(this.backwardParamsErrors)

    } else {
      paramsError = this.backwardParamsErrors
    }

    return paramsError
  }

  /**
   *
   */
  fun getInputErrors(copy: Boolean = true): DenseNDArray {
    require(!this.neuralNetwork.sparseInput) { "Input errors available only if input is dense" }

    return if (copy) {
      this.structure.inputLayer.inputArray.errors.copy()
    } else {
      this.structure.inputLayer.inputArray.errors
    }
  }

  /**
   * Get the relevance of the input.
   * (If the input is Dense it is Dense, if the input is Sparse or SparseBinary it is Sparse).
   *
   * @param copy whether to return a copy of the relevance or not
   *
   * @return the relevance of the input as [NDArray]
   */
  fun getInputRelevance(copy: Boolean = true): NDArray<*> {

    return if (copy) {
      this.structure.inputLayer.inputArray.relevance.values.copy()
    } else {
      this.structure.inputLayer.inputArray.relevance.values
    }
  }

  /**
   * Forward features.
   *
   * @param featuresArray the features to forward from the input to the output
   * @param useDropout whether to apply the dropout
   */
  fun forward(featuresArray: InputNDArrayType, useDropout: Boolean = false): DenseNDArray {

    this.structure.forward(features = featuresArray, useDropout = useDropout)

    return this.structure.outputLayer.outputArray.values
  }

  /**
   * Forward features, calculating their relevance respect of the output.
   *
   * @param featuresArray the features to forward from the input to the output
   * @param relevantOutcomesDistribution the distribution which indicates which outcomes are relevant, used
   *                                     as reference to calculate the relevance of the input
   * @param useDropout whether to apply the dropout
   *
   * @return the output [NDArray]
   */
  fun forward(featuresArray: InputNDArrayType,
              relevantOutcomesDistribution: DistributionArray,
              useDropout: Boolean = false): DenseNDArray {

    this.structure.forward(
      features = featuresArray,
      paramsContributes = this.forwardParamsContributes,
      relevantOutcomesDistribution = relevantOutcomesDistribution,
      useDropout = useDropout)

    return this.structure.outputLayer.outputArray.values
  }

  /**
   *
   * @param outputErrors the errors on the output of the network
   * @param propagateToInput propagateErrorsToInput
   * @return the avgLoss respect to the output of the network
   */
  fun backward(outputErrors: DenseNDArray,
               propagateToInput: Boolean = false) {
    this.structure.backward(
      outputErrors = outputErrors,
      paramsErrors = this.backwardParamsErrors,
      propagateToInput = propagateToInput)
  }
}
