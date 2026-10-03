package streetlight.server.daemon.agent

import ai.koog.embeddings.base.Vector
import streetlight.model.data.EventTag
import kotlin.math.exp
import kotlin.math.sqrt

/** A logistic regression deciding one tag, from the features of an event's vector. */
class TagModel(
    private val weights: DoubleArray,
    private val bias: Double,
) {
    /** The probability that the event of [features] carries this tag. */
    fun readProbability(features: DoubleArray): Double = sigmoid(weights.dot(features) + bias)
}

/**
 * The model of each tag that at least two of the examples carry and some do not, trained on the [features] of each
 * example and the [tagSets] it carries.
 */
fun trainTagModels(features: List<DoubleArray>, tagSets: List<List<EventTag>>): Map<EventTag, TagModel> =
    EventTag.entries.mapNotNull { tag ->
        val labels = tagSets.map { tag in it }
        val positives = labels.count { it }
        if (positives < minPositives || positives == labels.size) return@mapNotNull null
        tag to trainTagModel(features, labels)
    }.toMap()

/** The features of this vector: its difference from [center], scaled to unit length. */
fun Vector.toFeatures(center: DoubleArray): DoubleArray {
    val centered = DoubleArray(center.size) { values[it] - center[it] }
    val length = sqrt(centered.dot(centered))
    return DoubleArray(centered.size) { centered[it] / length }
}

/**
 * The regression of one tag, trained by gradient descent on the [labels] of [features], each class weighted by the
 * inverse of its share and the weights regularized.
 */
private fun trainTagModel(features: List<DoubleArray>, labels: List<Boolean>): TagModel {
    val count = features.size
    val size = features.first().size
    val positives = labels.count { it }
    val positiveWeight = count / (2.0 * positives)
    val negativeWeight = count / (2.0 * (count - positives))
    val weights = DoubleArray(size)
    var bias = 0.0

    repeat(trainingIterations) {
        val gradient = DoubleArray(size)
        var biasGradient = 0.0
        features.forEachIndexed { i, x ->
            val error = sigmoid(weights.dot(x) + bias) - if (labels[i]) 1.0 else 0.0
            val step = (if (labels[i]) positiveWeight else negativeWeight) * error / count
            for (k in 0 until size) gradient[k] += step * x[k]
            biasGradient += step
        }
        for (k in 0 until size) weights[k] -= learningRate * (gradient[k] + weights[k] / (regularization * count))
        bias -= learningRate * biasGradient
    }
    return TagModel(weights, bias)
}

private fun DoubleArray.dot(other: DoubleArray): Double {
    var sum = 0.0
    for (i in indices) sum += this[i] * other[i]
    return sum
}

private fun sigmoid(value: Double) = 1 / (1 + exp(-value))

private const val minPositives = 2
private const val trainingIterations = 400
private const val learningRate = 2.0
private const val regularization = 1.0
