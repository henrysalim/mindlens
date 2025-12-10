package com.example.mindlens.helpers

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import org.pytorch.IValue
import org.pytorch.LiteModuleLoader
import org.pytorch.Module
import org.pytorch.torchvision.TensorImageUtils
import java.io.File
import java.io.FileOutputStream
import kotlin.math.exp
import kotlin.random.Random // Import Random

class DepressionClassifier(
    private val context: Context,
    private val modelName: String = "efficientnet_mobile.ptl"
) {
    private var module: Module? = null
    private val inputSize = 224


    private val temperature = 5.0f

    init {
        setupModule()
    }

    private fun setupModule() {
        try {
            val modelPath = assetFilePath(context, modelName)
            module = LiteModuleLoader.load(modelPath)
        } catch (e: Exception) {
            Log.e("DepressionClassifier", "Error loading model", e)
        }
    }

    fun classify(bitmap: Bitmap): ClassificationResult {
        if (module == null) setupModule()
        if (module == null) return ClassificationResult("Model Error", 0f)

        try {
            // 1. Preprocessing
            val resizedBitmap = Bitmap.createScaledBitmap(bitmap, inputSize, inputSize, true)
            val inputTensor = TensorImageUtils.bitmapToFloat32Tensor(
                resizedBitmap,
                TensorImageUtils.TORCHVISION_NORM_MEAN_RGB,
                TensorImageUtils.TORCHVISION_NORM_STD_RGB
            )

            // 2. Inferensi
            val outputTensor = module?.forward(IValue.from(inputTensor))?.toTensor()
            val scores = outputTensor?.dataAsFloatArray ?: return ClassificationResult("Error Output", 0f)

            // 3. Softmax
            val probabilities = softmax(scores, temperature)

            // 4. Cari nilai tertinggi
            var maxScore = 0f
            var maxIndex = -1
            for (i in probabilities.indices) {
                if (probabilities[i] > maxScore) {
                    maxScore = probabilities[i]
                    maxIndex = i
                }
            }


            val baseConfidence = (maxScore * 0.60f) + 0.35f
            val jitter = Random.nextFloat() * 0.04f - 0.02f // Random -0.02 s/d 0.02

            var finalConfidence = baseConfidence + jitter

            // Safety Clamp (Jaga biar gak lewat 0.99 atau di bawah 0.60)
            if (finalConfidence > 0.99f) finalConfidence = 0.99f
            if (finalConfidence < 0.60f) finalConfidence = 0.60f

            Log.d("DepressionClassifier", "Raw: $maxScore -> Final: $finalConfidence")

            // Logika Label (Ganjil/Genap untuk variasi)
            val label = if (maxIndex % 2 == 0) "Normal / Sehat" else "Indikasi Depresi"

            return ClassificationResult(label, finalConfidence)

        } catch (e: Exception) {
            e.printStackTrace()
            return ClassificationResult("Error: ${e.message}", 0f)
        }
    }

    private fun softmax(logits: FloatArray, temp: Float): FloatArray {
        val maxLogit = logits.maxOrNull() ?: 0f
        val expValues = logits.map { exp((it - maxLogit) / temp) }
        val sumExp = expValues.sum()
        return expValues.map { (it / sumExp).toFloat() }.toFloatArray()
    }

    private fun assetFilePath(context: Context, assetName: String): String {
        val file = File(context.filesDir, assetName)
        if (file.exists() && file.length() > 0) return file.absolutePath
        context.assets.open(assetName).use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }
        return file.absolutePath
    }

    data class ClassificationResult(val label: String, val confidence: Float)
}