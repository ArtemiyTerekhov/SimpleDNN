/* Copyright 2016-present The KotlinNLP Authors. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, you can obtain one at http://mozilla.org/MPL/2.0/.
 * ------------------------------------------------------------------*/

package deeplearning

import deeplearning.utils.SlidingWindowSequenceUtils.buildSlidingWindowSequence
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.context
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import java.util.*
import kotlin.test.assertEquals

/**
 *
 */
class SlidingWindowSequenceSpec: Spek({

  describe("a SlidingWindowSequence") {

    context("size = 5, leftContextSize = 3, rightContextSize = 3") {

      val sequence = buildSlidingWindowSequence()

      on("setFocus on the 2nd element"){
        sequence.setFocus(2)

        it("should have the focus at the 2nd element") {
          assertEquals(2, sequence.focusIndex)
        }
      }

      on("first hasNext (focus = 0)") {
        sequence.setFocus(0)

        it("should return true") {
          assertEquals(true, sequence.hasNext())
        }
      }

      on("last hasNext (focus = 4)") {
        sequence.setFocus(4)

        it("should return false") {
          assertEquals(false, sequence.hasNext())
        }
      }

      on("shift 2nd element"){
        sequence.setFocus(2)

        sequence.shift()

        it("should have the focus on the 3th element") {
          assertEquals(3, sequence.focusIndex)
        }
      }

      on("getLeftContext() focus = 2"){
        sequence.setFocus(2)

        it("should have the expected values") {
          assertEquals(true, Arrays.equals(arrayOf(null, 0, 1), sequence.getLeftContext()))
        }
      }

      on("getRightContext() focus = 2"){
        sequence.setFocus(2)

        it("should have the expected values") {
          assertEquals(true, Arrays.equals(arrayOf(3, 4, null), sequence.getRightContext()))
        }
      }

      on("contextToString focus = 0"){
        sequence.setFocus(0)

        it("should have the expected window") {
          assertEquals(true, "[null, null, null] 0 [1, 2, 3]" == sequence.contextToString())
        }
      }

      on("contextToString focus = 1"){
        sequence.setFocus(1)

        it("should have the expected window") {
          assertEquals(true, "[null, null, 0] 1 [2, 3, 4]" == sequence.contextToString())
        }
      }

      on("contextToString focus = 2"){
        sequence.setFocus(2)

        it("should have the expected window") {
          assertEquals(true, "[null, 0, 1] 2 [3, 4, null]" == sequence.contextToString())
        }
      }

      on("contextToString focus = 3"){
        sequence.setFocus(3)

        it("should have the expected window") {
          assertEquals(true, "[0, 1, 2] 3 [4, null, null]" == sequence.contextToString())
        }
      }

      on("contextToString focus = 4"){
        sequence.setFocus(4)

        it("should have the expected window") {
          assertEquals(true, "[1, 2, 3] 4 [null, null, null]" == sequence.contextToString())
        }
      }
    }
  }
})