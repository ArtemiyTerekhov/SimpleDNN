/* Copyright 2016-present The KotlinNLP Authors. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, you can obtain one at http://mozilla.org/MPL/2.0/.
 * ------------------------------------------------------------------*/

package com.kotlinnlp.simplednn.simplemath

import com.kotlinnlp.simplednn.simplemath.ndarray.Shape
import com.kotlinnlp.simplednn.simplemath.ndarray.wrapper.JBlasArray

typealias NDArray = JBlasArray

/**
 * @param a a number
 * @param b a number
 * @param tolerance a must be in the range [b - tolerance, b + tolerance] to return True
 *
 * @return a Boolean which indicates if a is equal to be within the tolerance
 */
fun equals(a: Number, b: Number, tolerance: Double = 10e-4): Boolean {

  val lower = b.toDouble() - tolerance
  val upper = b.toDouble() + tolerance

  return a.toDouble() in lower..upper
}

fun concatVectorsV(vararg vectors: NDArray): NDArray {

  require(vectors.all { it.isVector && it.columns == 1 })

  val array = JBlasArray.zeros(Shape(vectors.sumBy { it.length }))

  var i = 0

  vectors.forEach {
    (0 until it.length).forEach { j -> array[i++] = it[j] }
  }

  return array

}