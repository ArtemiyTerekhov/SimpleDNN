/* Copyright 2016-present The KotlinNLP Authors. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, you can obtain one at http://mozilla.org/MPL/2.0/.
 * ------------------------------------------------------------------*/

package updatemethods

import com.kotlinnlp.simplednn.core.arrays.UpdatableDenseArray
import com.kotlinnlp.simplednn.core.functionalities.updatemethods.nesterovmomentum.NesterovMomentumMethod
import com.kotlinnlp.simplednn.core.functionalities.updatemethods.nesterovmomentum.NesterovMomentumStructure
import com.kotlinnlp.simplednn.simplemath.ndarray.dense.DenseNDArrayFactory
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.context
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import kotlin.test.assertEquals

/**
 *
 */
class NesterovMomentumSpec: Spek({

  describe("the NesterovMomentum update method") {

    context("update with dense errors") {

      on("get support structure") {

        val updateHelper = NesterovMomentumMethod(learningRate = 0.001, momentum = 0.9)
        val updatableArray: UpdatableDenseArray = Utils.buildUpdateableArray()

        it("should return a support structure of the expected type") {
          assertEquals(true, updateHelper.getSupportStructure(updatableArray) is NesterovMomentumStructure)
        }
      }

      on("update") {

        val updateHelper = NesterovMomentumMethod(learningRate = 0.001, momentum = 0.9)
        val updatableArray: UpdatableDenseArray = Utils.buildUpdateableArray()
        val supportStructure = updateHelper.getSupportStructure(updatableArray) as NesterovMomentumStructure

        supportStructure.v.assignValues(Utils.supportArray1())

        updateHelper.update(array = updatableArray, errors = Utils.buildDenseErrors())

        it("should match the expected updated array") {
          assertEquals(true, updatableArray.values.equals(
            DenseNDArrayFactory.arrayOf(doubleArrayOf(-0.16871, -0.24933, 0.09424, 0.75548, 0.63781)),
            tolerance = 1.0e-6))
        }
      }
    }

    context("update with sparse errors") {

      on("update") {

        val updateHelper = NesterovMomentumMethod(learningRate = 0.001, momentum = 0.9)
        val updatableArray: UpdatableDenseArray = Utils.buildUpdateableArray()
        val supportStructure = updateHelper.getSupportStructure(updatableArray) as NesterovMomentumStructure

        supportStructure.v.assignValues(Utils.supportArray1())

        updateHelper.update(array = updatableArray, errors = Utils.buildSparseErrors())

        it("should match the expected updated array") {
          assertEquals(true, updatableArray.values.equals(
            DenseNDArrayFactory.arrayOf(doubleArrayOf(0.4, -0.24933, 0.5, 1.0, 0.63743)),
            tolerance = 1.0e-6))
        }
      }
    }
  }
})
