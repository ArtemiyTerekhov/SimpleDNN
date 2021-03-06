/* Copyright 2016-present The KotlinNLP Authors. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, you can obtain one at http://mozilla.org/MPL/2.0/.
 * ------------------------------------------------------------------*/

package com.kotlinnlp.simplednn.core.layers

import com.kotlinnlp.simplednn.simplemath.ndarray.NDArray
import com.kotlinnlp.simplednn.simplemath.ndarray.dense.DenseNDArray
import com.kotlinnlp.simplednn.simplemath.ndarray.dense.DenseNDArrayFactory
import com.kotlinnlp.simplednn.simplemath.ndarray.sparse.SparseNDArray
import com.kotlinnlp.simplednn.simplemath.ndarray.sparsebinary.SparseBinaryNDArray

/**
 * Utils for the calculation of the relevance.
 */
object RelevanceUtils {

  /**
   * The stabilizing term used to calculate the relevance
   */
  private val relevanceEps: Double = 0.01

  /**
   * Calculate the relevance of the array [x] respect of the calculation which produced the array [y].
   *
   * @param x a generic [NDArray]
   * @param y a [DenseNDArray] (no Sparse needed, generally little size on output)
   * @param yRelevance a [DenseNDArray], whose norm is 1.0, which indicates how much relevant are the values of [y]
   * @param contributions a matrix which maps the contributions from each value of [x] to each value of [y]
   *
   * @return the relevance of [x] respect of [y]
   */
  fun calculateRelevanceOfArray(x: NDArray<*>,
                                y: DenseNDArray,
                                yRelevance: DenseNDArray,
                                contributions: NDArray<*>): NDArray<*> =
    when (x) {

      is DenseNDArray -> this.calculateRelevanceOfDenseArray(
        x = x,
        y = y,
        yRelevance = yRelevance,
        contributions = contributions as DenseNDArray)

      is SparseBinaryNDArray -> this.calculateRelevanceOfSparseArray(
        x = x,
        y = y,
        yRelevance = yRelevance,
        contributions = contributions as SparseNDArray)

      else -> throw RuntimeException("Invalid input type '%s'".format(x.javaClass.name))
    }

  /**
   * Calculate the relevance of the Dense array [x] respect of the calculation which produced the Dense array [y].
   *
   * @param x a [DenseNDArray]
   * @param y a [DenseNDArray] (no Sparse needed, generally little size on output)
   * @param yRelevance a [DenseNDArray], whose norm is 1.0, which indicates how much relevant are the values of [y]
   * @param contributions a matrix which contains the contributions of each value of [x] to calculate each value of [y]
   *
   * @return the relevance of [x] respect of [y]
   */
  fun calculateRelevanceOfDenseArray(x: DenseNDArray,
                                     y: DenseNDArray,
                                     yRelevance: DenseNDArray,
                                     contributions: DenseNDArray): DenseNDArray {

    val relevanceArray: DenseNDArray = DenseNDArrayFactory.zeros(shape = x.shape)
    val xLength: Int = x.length
    val yLength: Int = y.length

    for (i in 0 until xLength) {

      for (j in 0 until yLength) {
        val eps: Double = if (y[j] >= 0) this.relevanceEps else -this.relevanceEps
        val epsN: Double = eps / xLength

        relevanceArray[i] += yRelevance[j] * (contributions[j, i]  + epsN) / (y[j] + eps)
      }
    }

    return relevanceArray
  }

  /**
   * Calculate the relevance of the SparseBinary array [x] respect of the calculation which produced the Dense array
   * [y].
   *
   * @param x a [SparseBinaryNDArray]
   * @param y a [DenseNDArray] (no Sparse needed, generally little size on output)
   * @param yRelevance a [DenseNDArray], whose norm is 1.0, which indicates how much relevant are the values of [y]
   * @param contributions a matrix which contains the contributions of each value of [x] to calculate each value of [y]
   *
   * @return the relevance of [x] respect of [y]
   */
  private fun calculateRelevanceOfSparseArray(x: SparseBinaryNDArray,
                                              y: DenseNDArray,
                                              yRelevance: DenseNDArray,
                                              contributions: SparseNDArray): SparseNDArray {

    val xActiveIndices: List<Int> = x.activeIndicesByColumn.values.first()!!
    val xActiveIndicesSize: Int =  xActiveIndices.size
    val relevanceValues: Array<Double> = Array(size = xActiveIndicesSize, init = { 0.0 })
    val relevanceColumns: Array<Int> = Array(size = xActiveIndicesSize, init = { 0 })
    val relevanceRows: Array<Int> = xActiveIndices.toTypedArray()
    val yLength: Int = y.length
    var k: Int = 0

    for (l in 0 until xActiveIndicesSize) {
      // the indices of the non-zero elements of x are the same of the non-zero columns of contributions
      for (j in 0 until yLength) {
        // loop over the i-th column of contributions (which is non-zero)
        val eps: Double = if (y[j] >= 0) this.relevanceEps else -this.relevanceEps
        val epsN: Double = eps / xActiveIndicesSize
        val wContrJI: Double = contributions.values[k++]  // linear indexing

        relevanceValues[l] += yRelevance[j] * (wContrJI + epsN) / (y[j] + eps)
      }
    }

    return SparseNDArray(
      shape = x.shape,
      values = relevanceValues,
      rows = relevanceRows,
      columns = relevanceColumns
    )
  }

  /**
   * @param yRelevance the relevance of [y]
   * @param y the output array
   * @param yContribute1 the first contribution to calculate [y]
   * @param yContribute2 the second contribution to calculate [y]
   *
   * @return the partition of [yRelevance] with the same ratio as [yContribute1] is in respect of [y].
   */
  fun getRelevancePartition1(yRelevance: DenseNDArray,
                             y: DenseNDArray,
                             yContribute1: DenseNDArray,
                             yContribute2: DenseNDArray): DenseNDArray {

    val eps: DenseNDArray = yContribute2.nonZeroSign().assignProd(this.relevanceEps) // the same factor (yContribute2)
    // is needed to calculate eps either for the first partition then the second one

    // partition factor = (yContribute1 + eps / 2) / (yContribute1 + yContribute2 + eps) [eps avoids divisions by zero]
    return yRelevance.prod(yContribute1.sum(eps.div(2.0))).assignDiv(y.sum(eps))
  }

  /**
   * @param yRelevance the relevance of [y]
   * @param y the output array
   * @param yContribute2 the second contribution to calculate [y]
   *
   * @return the partition of [yRelevance] with the same ratio as [yContribute2] is in respect of [y].
   */
  fun getRelevancePartition2(yRelevance: DenseNDArray,
                             y: DenseNDArray,
                             yContribute2: DenseNDArray): DenseNDArray {

    val eps: DenseNDArray = yContribute2.nonZeroSign().assignProd(this.relevanceEps)

    // partition factor = (yContribute2 + eps / 2) / (yInput + yContribute2 + eps) [eps avoids divisions by zero]
    return yRelevance.prod(yContribute2.sum(eps.div(2.0))).assignDiv(y.sum(eps))
  }
}
