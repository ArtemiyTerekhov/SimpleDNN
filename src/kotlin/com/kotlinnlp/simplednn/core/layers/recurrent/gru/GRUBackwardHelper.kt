/* Copyright 2016-present The KotlinNLP Authors. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, you can obtain one at http://mozilla.org/MPL/2.0/.
 * ------------------------------------------------------------------*/

package com.kotlinnlp.simplednn.core.layers.recurrent.gru

import com.kotlinnlp.simplednn.core.arrays.AugmentedArray
import com.kotlinnlp.simplednn.core.layers.BackwardHelper
import com.kotlinnlp.simplednn.core.layers.LayerParameters
import com.kotlinnlp.simplednn.simplemath.ndarray.NDArray
import com.kotlinnlp.simplednn.simplemath.ndarray.dense.DenseNDArray

/**
 * The helper which executes the backward on a [layer].
 *
 * @property layer the [GRULayerStructure] in which the backward is executed
 */
class GRUBackwardHelper<InputNDArrayType : NDArray<InputNDArrayType>>(
  override val layer: GRULayerStructure<InputNDArrayType>
) : BackwardHelper<InputNDArrayType> {

  /**
   * A support variable to manage the errors on the parameters during the backward
   */
  lateinit private var paramsErrors: GRULayerParameters

  /**
   * Executes the backward calculating the errors of the parameters and eventually of the input through the SGD
   * algorithm, starting from the preset errors of the output array.
   *
   * @param paramsErrors the errors of the parameters which will be filled
   * @param propagateToInput whether to propagate the errors to the input array
   */
  override fun backward(paramsErrors: LayerParameters, propagateToInput: Boolean) {

    this.paramsErrors = paramsErrors as GRULayerParameters

    val prevStateOutput = this.layer.layerContextWindow.getPrevStateLayer()?.outputArray
    val nextStateLayer = this.layer.layerContextWindow.getNextStateLayer()

    this.addOutputRecurrentGradients(nextStateLayer as? GRULayerStructure<*>)

    this.assignGatesGradients(prevStateOutput)
    this.assignParamsGradients(prevStateOutput)

    if (propagateToInput) {
      this.assignLayerGradients()
    }
  }

  /**
   *
   * @param prevStateOutput the outputArray in the previous state
   */
  private fun assignGatesGradients(prevStateOutput: AugmentedArray<DenseNDArray>?) {
    this.layer.params as GRULayerParameters

    val gy: DenseNDArray = this.layer.outputArray.errors

    val resetGate = this.layer.resetGate
    val partitionGate = this.layer.partitionGate
    val candidate = this.layer.candidate

    val p: DenseNDArray = partitionGate.values
    val c: DenseNDArray = candidate.values

    val rDeriv: DenseNDArray = resetGate.calculateActivationDeriv()
    val pDeriv: DenseNDArray = partitionGate.calculateActivationDeriv()
    val cDeriv: DenseNDArray = candidate.calculateActivationDeriv()

    val gr: DenseNDArray = this.layer.resetGate.errors
    val gp: DenseNDArray = this.layer.partitionGate.errors
    val gc: DenseNDArray = this.layer.candidate.errors

    gc.assignProd(p, cDeriv).assignProd(gy)  // gc must be calculated before gr and gp

    if (prevStateOutput == null) {
      gr.zeros()
      gp.assignProd(c, pDeriv).assignProd(gy)

    } else { // recurrent contribution

      val yPrev: DenseNDArray = prevStateOutput.values
      val wcr: DenseNDArray = this.layer.params.candidate.recurrentWeights.values

      gr.assignValues(gc.T.dot(wcr)).assignProd(rDeriv).assignProd(yPrev)
      gp.assignProd(c.sub(yPrev), pDeriv).assignProd(gy)
    }
  }

  /**
   *
   * @param prevStateOutput the outputArray in the previous state
   */
  private fun assignParamsGradients(prevStateOutput: AugmentedArray<DenseNDArray>?) {

    val x: InputNDArrayType = this.layer.inputArray.values
    val yPrev: DenseNDArray? = prevStateOutput?.values

    this.layer.resetGate.assignParamsGradients(paramsErrors = this.paramsErrors.resetGate, x = x, yPrev = yPrev)
    this.layer.partitionGate.assignParamsGradients(paramsErrors = this.paramsErrors.partitionGate, x = x, yPrev = yPrev)
    this.layer.candidate.assignParamsGradients(paramsErrors = this.paramsErrors.candidate, x = x)

    if (yPrev != null) { // add recurrent contribution to the recurrent weights of the candidate
      val r: DenseNDArray = this.layer.resetGate.values
      val gwcr: DenseNDArray = this.paramsErrors.candidate.recurrentWeights.values
      val gc: DenseNDArray = this.layer.candidate.errors
      gwcr.assignDot(gc, r.prod(yPrev).T)
    }
  }

  /**
   *
   */
  private fun assignLayerGradients() { this.layer.params as GRULayerParameters

    val gx: DenseNDArray = this.layer.inputArray.errors

    val wp: DenseNDArray = this.layer.params.partitionGate.weights.values as DenseNDArray
    val wc: DenseNDArray = this.layer.params.candidate.weights.values as DenseNDArray
    val wr: DenseNDArray = this.layer.params.resetGate.weights.values as DenseNDArray

    val gp: DenseNDArray = this.layer.partitionGate.errors
    val gc: DenseNDArray = this.layer.candidate.errors
    val gr: DenseNDArray = this.layer.resetGate.errors

    gx.assignValues(gp.T.dot(wp)).assignSum(gc.T.dot(wc)).assignSum(gr.T.dot(wr))

    if (this.layer.inputArray.hasActivation && gx is DenseNDArray) {
      gx.assignProd(this.layer.inputArray.calculateActivationDeriv())
    }
  }

  /**
   *
   * @param nextStateLayer the layer structure in the next state
   */
  private fun addOutputRecurrentGradients(nextStateLayer: GRULayerStructure<*>?) {

    if (nextStateLayer != null) {
      val gy: DenseNDArray = this.layer.outputArray.errors
      val gyRec: DenseNDArray = this.getLayerRecurrentContribution(nextStateLayer)

      gy.assignSum(gyRec)
    }
  }

  /**
   *
   * @param nextStateLayer the layer structure in the next state
   */
  private fun getLayerRecurrentContribution(nextStateLayer: GRULayerStructure<*>): DenseNDArray {
    this.layer.params as GRULayerParameters

    val resetGate = nextStateLayer.resetGate
    val partitionGate = nextStateLayer.partitionGate
    val candidate = nextStateLayer.candidate

    val gy: DenseNDArray = nextStateLayer.outputArray.errors

    val r: DenseNDArray = resetGate.values
    val p: DenseNDArray = partitionGate.values

    val gr: DenseNDArray = resetGate.errors
    val gp: DenseNDArray = partitionGate.errors
    val gc: DenseNDArray = candidate.errors

    val wrr: DenseNDArray = this.layer.params.resetGate.recurrentWeights.values
    val wpr: DenseNDArray = this.layer.params.partitionGate.recurrentWeights.values
    val wcr: DenseNDArray = this.layer.params.candidate.recurrentWeights.values

    val gRec1: DenseNDArray = gr.T.dot(wrr)
    val gRec2: DenseNDArray = gp.T.dot(wpr)
    val gRec3: DenseNDArray = gc.T.dot(wcr).prod(r)
    val gRec4: DenseNDArray = p.reverseSub(1.0).assignProd(gy).T

    return gRec1.assignSum(gRec2).assignSum(gRec3).assignSum(gRec4)
  }
}
