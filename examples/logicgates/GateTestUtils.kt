/* Copyright 2016-present The KotlinNLP Authors. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, you can obtain one at http://mozilla.org/MPL/2.0/.
 * ------------------------------------------------------------------*/

package logicgates

import com.kotlinnlp.simplednn.core.functionalities.activations.ELU
import com.kotlinnlp.simplednn.core.optimizer.ParamsOptimizer
import com.kotlinnlp.simplednn.core.functionalities.activations.Sigmoid
import com.kotlinnlp.simplednn.core.functionalities.activations.Softmax
import com.kotlinnlp.simplednn.helpers.training.FeedforwardTrainingHelper
import com.kotlinnlp.simplednn.core.functionalities.decaymethods.HyperbolicDecay
import com.kotlinnlp.simplednn.core.functionalities.updatemethods.learningrate.LearningRateMethod
import com.kotlinnlp.simplednn.core.functionalities.losses.MSECalculator
import com.kotlinnlp.simplednn.core.neuralnetwork.NeuralNetwork
import com.kotlinnlp.simplednn.core.neuralnetwork.preset.FeedforwardNeuralNetwork
import com.kotlinnlp.simplednn.core.neuralprocessor.feedforward.FeedforwardNeuralProcessor
import com.kotlinnlp.simplednn.dataset.Shuffler
import com.kotlinnlp.simplednn.dataset.SimpleExample
import com.kotlinnlp.simplednn.core.functionalities.outputevaluation.ClassificationEvaluation
import com.kotlinnlp.simplednn.core.functionalities.outputevaluation.MulticlassEvaluation
import com.kotlinnlp.simplednn.core.functionalities.outputevaluation.OutputEvaluationFunction
import com.kotlinnlp.simplednn.helpers.validation.FeedforwardValidationHelper
import com.kotlinnlp.simplednn.simplemath.ndarray.dense.DenseNDArray

object GateTestUtils {

  /**
   *
   */
  fun testAccuracyWithSoftmax(inputSize: Int, examples: ArrayList<SimpleExample<DenseNDArray>>, epochs: Int): Double {

    val neuralNetwork = this.buildSoftmaxNetwork(inputSize)

    return this.testAccuracy(
      neuralNetwork = neuralNetwork,
      examples = examples,
      evaluationFunction = ClassificationEvaluation(),
      epochs = epochs)
  }

  /**
   *
   */
  fun testAccuracyWithSigmoid(inputSize: Int, examples: ArrayList<SimpleExample<DenseNDArray>>, epochs: Int): Double {

    val neuralNetwork = this.buildSigmoidNetwork(inputSize)

    return this.testAccuracy(
      neuralNetwork = neuralNetwork,
      examples = examples,
      evaluationFunction = MulticlassEvaluation(),
      epochs = epochs)
  }

  /**
   *
   */
  private fun buildSigmoidNetwork(inputSize: Int): NeuralNetwork {

    val nn = FeedforwardNeuralNetwork(
      inputSize = inputSize,
      hiddenSize = 10,
      hiddenActivation = ELU(),
      outputSize = 1,
      outputActivation = Sigmoid())

    nn.initialize()

    return nn
  }

  /**
   *
   */
  private fun buildSoftmaxNetwork(inputSize: Int): NeuralNetwork {

    val nn = FeedforwardNeuralNetwork(
      inputSize = inputSize,
      hiddenSize = 10,
      hiddenActivation = ELU(),
      outputSize = 2,
      outputActivation = Softmax())

    nn.initialize()

    return nn
  }

  /**
   *
   */
  private fun testAccuracy(neuralNetwork: NeuralNetwork,
                           examples: ArrayList<SimpleExample<DenseNDArray>>,
                           evaluationFunction: OutputEvaluationFunction,
                           epochs: Int): Double {

    val updateMethod = LearningRateMethod(
        learningRate = 0.01,
        decayMethod = HyperbolicDecay(decay = 0.0, initLearningRate = 0.01))

    val optimizer = ParamsOptimizer(
      neuralNetwork = neuralNetwork,
      updateMethod = updateMethod)

    val neuralProcessor = FeedforwardNeuralProcessor<DenseNDArray>(neuralNetwork)

    val trainingHelper = FeedforwardTrainingHelper(
      neuralProcessor = neuralProcessor,
      optimizer = optimizer,
      lossCalculator = MSECalculator())

    val validationHelper =  FeedforwardValidationHelper(
        neuralProcessor = neuralProcessor,
        outputEvaluationFunction = evaluationFunction)

    trainingHelper.train(
      trainingExamples = examples,
      validationExamples = examples,
      epochs = epochs,
      batchSize = 1,
      shuffler = Shuffler(enablePseudoRandom = true, seed = 1),
      validationHelper = validationHelper)

    return trainingHelper.statistics.lastAccuracy
  }

}
