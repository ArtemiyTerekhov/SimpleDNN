/* Copyright 2016-present The KotlinNLP Authors. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, you can obtain one at http://mozilla.org/MPL/2.0/.
 * ------------------------------------------------------------------*/

package com.kotlinnlp.simplednn.core.neuralnetwork

import com.kotlinnlp.simplednn.core.layers.LayerConfiguration
import com.kotlinnlp.simplednn.core.functionalities.randomgenerators.FixedRangeRandom
import com.kotlinnlp.simplednn.core.functionalities.randomgenerators.RandomGenerator
import com.kotlinnlp.simplednn.core.layers.LayerType
import com.kotlinnlp.simplednn.utils.Serializer
import java.io.InputStream
import java.io.OutputStream
import java.io.Serializable

/**
 * The NeuralNetwork.
 *
 * The topology of the Neural Network refers to the way the Neurons are connected each other.
 * This information is contained in the [layersConfiguration].
 *
 * @property layersConfiguration a list of configurations, one per layer
 */
class NeuralNetwork(val layersConfiguration: List<LayerConfiguration>) : Serializable {

  /**
   * Secondary constructor
   *
   * @param layerConfiguration the layersConfiguration of a layer
   * @return a new NeuralNetwork
   */
  constructor(vararg layerConfiguration: LayerConfiguration): this(layersConfiguration = layerConfiguration.toList())

  companion object {

    /**
     * Private val used to serialize the class (needed from Serializable)
     */
    @Suppress("unused")
    private const val serialVersionUID: Long = 1L

    /**
     * Read a [NeuralNetwork] (serialized) from an input stream and decode it.
     *
     * @param inputStream the [InputStream] from which to read the serialized [NeuralNetwork]
     *
     * @return the [NeuralNetwork] read from [inputStream] and decoded
     */
    fun load(inputStream: InputStream): NeuralNetwork = Serializer.deserialize(inputStream)
  }

  /**
   *
   */
  val inputType: LayerType.Input = this.layersConfiguration.first().inputType

  /**
   *
   */
  val sparseInput: Boolean = this.inputType == LayerType.Input.SparseBinary

  /**
   * Contains the parameters of each layer which can be trained
   */
  val model = this.parametersFactory()

  /**
   * Serialize this [NeuralNetwork] and write it to an output stream.
   *
   * @param outputStream the [OutputStream] in which to write this serialized [NeuralNetwork]
   */
  fun dump(outputStream: OutputStream) = Serializer.serialize(this, outputStream)

  /**
   * Initialize the weights and biases of the model
   *
   * @param randomGenerator the random generator
   * @param biasesInitValue the initial biases values (default 0)
   *
   * @return this network
   */
  fun initialize(randomGenerator: RandomGenerator = FixedRangeRandom(radius = 0.08, enablePseudoRandom = true),
                 biasesInitValue: Double = 0.0): NeuralNetwork {

    this.model.initialize(
      randomGenerator = randomGenerator,
      biasesInitValue = biasesInitValue)

    return this
  }

  /**
   * Generate [NetworkParameters] compatible with the configuration of this network
   */
  fun parametersFactory() = NetworkParameters(
    layersConfiguration = this.layersConfiguration,
    sparseInput = false)

  /**
   * Generate [NetworkParameters] used to store errors, compatible with the configuration of this network
   */
  fun parametersErrorsFactory() = NetworkParameters(
    layersConfiguration = this.layersConfiguration,
    sparseInput = this.sparseInput)
}
