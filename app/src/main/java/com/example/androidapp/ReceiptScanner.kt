package com.example.androidapp
import android.graphics.Bitmap
import android.graphics.Point
import android.util.Log
import androidx.camera.core.ImageProxy
import androidx.core.text.isDigitsOnly
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class ReceiptScanner private constructor() {

    private var textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    private var diff = 0.0

    private var total = 0.0
    companion object {

        @Volatile
        private var instance: ReceiptScanner? = null

        fun getInstance() =
            instance ?: synchronized(this) {
                instance ?: ReceiptScanner().also { instance = it }
            }
    }

    fun parseReceiptImage(imageBitmap: Bitmap?): List<Pair<String, Pair<Double, String>>>? {
        val image = imageBitmap?.let { InputImage.fromBitmap(it, 0) }
        var receiptParsedResults : List<Pair<String, Pair<Double, String>>>? = null
        image?.let {
            textRecognizer!!.process(it)
                .addOnSuccessListener { text ->
                    receiptParsedResults = parseText(text, it.width)
                }
                .addOnFailureListener { e ->
                    Log.e("ReceiptParser", "Error parsing receipt image: ${e.message}", e)
                }
        }
        return receiptParsedResults
    }

    //@ExperimentalGetImage
    fun parseReceiptMediaImage(imageProxy: ImageProxy?, callback : (List<Pair<String, Pair<Double, String>>>?) -> Unit) {
        val image = imageProxy?.let { InputImage.fromMediaImage(it.image!!, imageProxy.imageInfo.rotationDegrees) }
        var receiptParsedResults : List<Pair<String, Pair<Double, String>>>? = null
        image?.let {
            textRecognizer!!.process(it)
                .addOnSuccessListener { text ->
                    receiptParsedResults = parseText(text, it.width)
                    callback(receiptParsedResults)
                }
                .addOnFailureListener { e ->
                    Log.e("ReceiptParser", "Error parsing receipt image: ${e.message}", e)
                }
        }
    }

    private fun parseText(text: Text, width: Int) : List<Pair<String, Pair<Double, String>>>{
        val blocks = text.textBlocks

        val receiptLines = getSortedReceiptLineList(blocks)

        val receiptLinesSorted = extractReceiptBlocks(receiptLines, 8.0, width)

        val cleanReceiptLinesSorted = cleanedReceiptLines(receiptLinesSorted)

        getProductNameAndQuantity(cleanReceiptLinesSorted)

        printReceiptBlocks(cleanReceiptLinesSorted)

        val products = getProductNameAndQuantity(cleanReceiptLinesSorted)

        printReceiptProducts(products)

        printAccuracy()

        return products
    }

    private fun getProductNameAndQuantity(receipt : List<List<Text.Element>>) : List<Pair<String, Pair<Double, String>>> {

        var product = mutableListOf<Pair<String, Pair<Double, String>>>()

        receipt.forEach { block ->
            var line = ""
            block.forEach {
                line += it.text + " "
            }

            val name = getProductName(line)
            val quant = getProductQuantity(line)

            if (name.isNotEmpty()) {
                product.add(Pair(name, quant))
            }
        }

        return product
    }

    private fun getProductName(text : String) : String {
        var name = ""
        val pattern = Regex("\\b[a-zA-Z]+\\b")
        var match = pattern.findAll(text).map { it.value }.toList()

        name = ""

        match.forEach {
            name += it + " "
        }

        name.dropLast(1)

        return name
    }

    private fun getProductQuantity(text : String) : Pair<Double, String> {
        var numericQuantity = 1.0

        val pattern = "(\\d+(?:\\.\\d+)?)(\\s*?)((?:grams|CT|dozens|gm|kg|kilogram|kilo gram| packs| kgram|packets|pair| ounce|spoon|piece))".toRegex()
        //improve
        val pattern2 = "^\\d+\\s*".toRegex()

        val getUnit = Regex("[\\s\\p{P}\\d]")
        val getQuantity = Regex("[\\s\\p{L}]")

        var match = pattern.find(text)?.value

        if (match.isNullOrEmpty()) {
            match = pattern2.find(text)?.value
        }

        var unit = match
        unit = unit?.replace(getUnit, "")

        var quantity = match
        quantity = quantity?.replace(getQuantity, "")

        if (unit.isNullOrEmpty()) {
            unit = "unit"
        }

        if (!quantity.isNullOrEmpty()) {
            numericQuantity = quantity.toDouble()
        }

        return Pair(numericQuantity, unit)
    }

    private fun satisfiesRule(word : String) : Boolean {
        var isSatisfied = true;

        val fourConsecCharPattern = Regex("(\\w)\\1{3}")

        val size = word.length

        if (size > 40) {
            isSatisfied = false;
        }

        if (word.isDigitsOnly() && (size > 3)) {
            isSatisfied = false;
        }

        if (!word.isDigitsOnly() && (size < 3)) {
            isSatisfied = false;
        }

        if (fourConsecCharPattern.containsMatchIn(word)) {
            isSatisfied = false;
        }

        if (word.contains("*")) {
            isSatisfied = false;
        }

        val alphaNumericWord = word.filter { it.isLetterOrDigit() }
        val alphaNumericDigits = alphaNumericWord.filter { it.isDigit()}

        if (alphaNumericDigits.length > 5) {
            isSatisfied = false;
        }

        val lettersCount = alphaNumericWord.count { it.isLetter() }
        val numbersCount = alphaNumericWord.count { it.isDigit() }
        var ratio = 0.0

        if (lettersCount > 0) {
            ratio = (numbersCount / lettersCount).toDouble()
        }

        if (ratio > 1) {
            isSatisfied = false;
        }

        return isSatisfied
    }

    private fun removeReceiptMetaData(receipt : List<List<Text.Element>>) : List<List<Text.Element>> {
        val indexToRemoveMetaDataFooter = mutableListOf<Int>()
        val indexToRemoveMetaDataHeader = mutableListOf<Int>()

        var blocks = receipt

        val footerConstantWords = listOf<String>("TOTAL", "SUBTOTAL", "TAX")
        val headerConstantWords = listOf<String>("Member", "ST#", "TR#", "E&OE", "Served", "Sobeys", "Costco", "Walmart", "Basics")

        var index = 0;
        blocks.forEach { block ->
            for (word in block) {
                if (footerConstantWords.any { word.text.contains(it) }) {
                    indexToRemoveMetaDataFooter.add(index)
                }
                if (headerConstantWords.any { word.text.lowercase().contains(it.lowercase()) }) {
                    indexToRemoveMetaDataHeader.add(index)
                }
            }

            index += 1;
        }

        if (indexToRemoveMetaDataFooter.isNotEmpty()) {
            val minRemoveIndex = indexToRemoveMetaDataFooter.min()
            blocks = blocks.slice(0 until minRemoveIndex)
        }

        if (indexToRemoveMetaDataHeader.isNotEmpty()) {
            val maxRemoveIndex = indexToRemoveMetaDataHeader.max()
            blocks = blocks.slice(maxRemoveIndex + 1 until blocks.size)
        }

        return blocks
    }

    private fun cleanedReceiptLines(receipt : List<List<Text.Element>>) : List<List<Text.Element>> {

        var blocks = mutableListOf<List<Text.Element>>()

        val receiptWithoutMetaData = removeReceiptMetaData(receipt)

        receiptWithoutMetaData.forEach {block ->
            var cleanBlock = mutableListOf<Text.Element>()

            for (word in block) {
                if (satisfiesRule(word.text)) {
                    cleanBlock.add(word)
                }
            }

            if (cleanBlock.isNotEmpty()) {
                blocks.add(cleanBlock)
            }
        }

        return blocks;
    }

    private fun getSortedReceiptLineList(blocks: List<Text.TextBlock>): List<Text.Element> {
        var lineList = mutableListOf<Text.Element>()

        for (block in blocks) {
            for (line in block.lines) {
                for (element in line.elements) {
                    lineList.add(element)
                }
            }
        }
        return lineList.sortedWith(compareBy({ it.cornerPoints?.get(0)!!.y }, { it.cornerPoints?.get(0)!!.x }));
    }

    private fun sum(point : Point?) : Double {
        var value = 0.0
        if (point != null) {
            value = point.x.toDouble() + (point.y * 5).toDouble();
            return value
        }
        return value
    }

    private fun minus(point : Point?) : Double {
        var value = 0.0
        if (point != null) {
            value = (point.x - point.y).toDouble()
            return value
        }
        return value
    }

    private fun updateAccuracyCheck(pointX : Double, pointY : Double, topLeftPointX : Double, topLeftPointY : Double, imgWidth : Double, interpolantDistance : Double) {
        val normal = pDistance(pointX, pointY, topLeftPointX, topLeftPointY, imgWidth, topLeftPointY)
        diff += (normal - interpolantDistance)
        total += normal;
    }

    private fun blockExtractInstrumentation(pointText : String, pointX : Double, pointY : Double, topLeftPointX : Double, topLeftPointY : Double, imgWidth : Double, interpolantDistance : Double) {
        Log.i("interpolate distance", interpolantDistance.toString())
        Log.i("non-distance", pDistance(pointX, pointY, topLeftPointX, topLeftPointY, imgWidth, topLeftPointY).toString())
        Log.i("point", pointText)
        Log.i("point Point", "(${pointX}, ${pointY})")
    }

    private fun extractReceiptBlock(points : MutableList<Text.Element>, block: MutableList<Text.Element>, imgWidth : Double, topLeftText : Text.Element, threshold : Double, interpolantOption : Int, isSecondPass : Boolean) : MutableList<Text.Element> {
        val remainingPoints= mutableListOf<Text.Element>()

        var topLeftPointX = block[0].cornerPoints?.get(0)!!.x.toDouble()
        var topLeftPointY = block[0].cornerPoints?.get(0)!!.y.toDouble()

        var imgWidthInterpolant = if (!isSecondPass) {
            val topRightPointX = block[0].cornerPoints?.get(1)!!.x.toDouble()
            val topRightPointY = block[0].cornerPoints?.get(1)!!.y.toDouble()
            val topLeftTextUpperPoints = listOf(Point(topLeftPointX.toInt(), topLeftPointY.toInt()), Point(topRightPointX.toInt(), topRightPointY.toInt()))

            lagrange_interpolate(topLeftTextUpperPoints, imgWidth, topLeftTextUpperPoints.size)

        } else {
            getInterpolantValueAtX(block, interpolantOption, imgWidth)
        }

        for (point in points) {
            val pointX = point.cornerPoints?.get(0)!!.x.toDouble()
            val pointY = point.cornerPoints?.get(0)!!.y.toDouble()
            val distance = pDistance(pointX, pointY, topLeftPointX, topLeftPointY, imgWidth, imgWidthInterpolant)

            if (point.text == "2.29") {
                blockExtractInstrumentation(point.text, pointX, pointY, topLeftPointX, topLeftPointY, imgWidth, distance)
            }

            if (distance.toInt() <= threshold) {
                blockExtractInstrumentation(point.text, pointX, pointY, topLeftPointX, topLeftPointY, imgWidth, distance)
                updateAccuracyCheck(pointX, pointY, topLeftPointX, topLeftPointY, imgWidth, distance)
                block.add(point)

                val blockSize = block.size
                if (blockSize >= 2) {
                    block.sortWith(compareBy { it.cornerPoints?.get(0)!!.x })
                    imgWidthInterpolant = getInterpolantValueAtX(block, interpolantOption, imgWidth);
                    topLeftPointX = block[blockSize - 1].cornerPoints?.get(0)!!.x.toDouble()
                    topLeftPointY = block[blockSize - 1].cornerPoints?.get(0)!!.y.toDouble()
                }
            } else {
                remainingPoints.add(point)
            }
        }

        return remainingPoints
    }

    private fun extractReceiptBlocks(points: List<Text.Element>, radius: Double, width : Int): List<List<Text.Element>> {
        val blocks = mutableListOf<List<Text.Element>>()
        var remainingPointsToGroup = points.toMutableList()

        while (remainingPointsToGroup.isNotEmpty()) {
            // Find top left point
            var block = mutableListOf<Text.Element>()
            val topLeftPoint = remainingPointsToGroup.minByOrNull {  sum(it.cornerPoints?.get(0)) }
            topLeftPoint?.let {topLeftText ->

                block.add(topLeftText)
                remainingPointsToGroup.remove(topLeftText)

                var threshold = radius;

                remainingPointsToGroup = extractReceiptBlock(remainingPointsToGroup, block, width.toDouble(), topLeftText, threshold, 1, false)

                if (block.size >= 2) {
                    remainingPointsToGroup = extractReceiptBlock(remainingPointsToGroup, block, width.toDouble(), topLeftText, threshold, 2, true)
                }

                block.sortWith(compareBy { it.cornerPoints?.get(0)!!.x })
                blocks.add(block)
            }
        }

        return blocks
    }

    private fun getInterpolantValueAtX(block : List<Text.Element>, interpolantOption : Int, x : Double) : Double {
        val blockSize = block.size
        val leftMostPoint = block[0];
        val leftEndPoint = block[blockSize - 2]
        val rightEndPoint = block[blockSize - 1]

        when (interpolantOption) {
            1 -> {
                return linear_interpolate(leftMostPoint, rightEndPoint, x)
            }

            2 -> {
                return linear_interpolate(leftMostPoint, getAveragePoint(block), x)
            }

            3 -> {
                return linear_interpolate(leftEndPoint, rightEndPoint, x)
            }

            4 -> {
                return lagrange_interpolate_text(block, x, blockSize)
            }

            5 -> {
                val leftEndPointX = leftEndPoint.cornerPoints?.get(0)!!.x
                val leftEndPointY = leftEndPoint.cornerPoints?.get(0)!!.y
                val rightEndPointX = rightEndPoint.cornerPoints?.get(0)!!.x
                val rightEndPointY = rightEndPoint.cornerPoints?.get(0)!!.y
                val knownPoints = listOf(Point(leftEndPointX, leftEndPointY), Point(rightEndPointX, rightEndPointY))
                return lagrange_interpolate(knownPoints, x, knownPoints.size)
            }
        }

        return 0.0
    }
    private fun pDistance(x : Double, y : Double, x1 : Double, y1 : Double, x2 : Double, y2 : Double) : Double {
        var A = x2 - x1;
        var B = y2 - y1;
        var C = y1 - y;
        var D = x1 - x;

        var AC = A * C;
        var DB = D * B;
        var diff = AC - DB;

        val AA = A * A;
        val BB = B * B;

        var squareSum = AA + BB;

        return Math.abs(diff) / Math.sqrt(squareSum)
    }

    private fun lagrange_interpolate(f: List<Point>, xi: Double, n: Int): Double {
        var result = 0.0 // Initialize result
        for (i in 0 until n) {
            // Compute individual terms of above formula
            var term: Double = f[i].y.toDouble()
            for (j in 0 until n) {
                val fjx = f[j].x
                val fix = f[i].x
                if (j != i) term *= ((xi - fjx) / (fix - fjx))
            }
            result += term
        }
        return result
    }


    private fun lagrange_interpolate_text(f: List<Text.Element>, xi: Double, n: Int): Double {
        var result = 0.0 // Initialize result
        for (i in 0 until n) {
            // Compute individual terms of above formula
            var term: Double = f[i].cornerPoints?.get(0)!!.y.toDouble()
            for (j in 0 until n) {
                val fjx = f[j].cornerPoints?.get(0)!!.x.toDouble()
                val fix = f[i].cornerPoints?.get(0)!!.x.toDouble()
                if (j != i) term *= ((xi - fjx) / (fix - fjx))
            }

            // Add current term to result
            result += term
        }
        return result
    }

    private fun linear_interpolate(a : Text.Element, b : Text.Element, xi : Double) : Double {

        val x1 = a.cornerPoints?.get(0)!!.x.toDouble()
        val y1 = a.cornerPoints?.get(0)!!.y.toDouble()
        val x2 = b.cornerPoints?.get(0)!!.x.toDouble()
        val y2 = b.cornerPoints?.get(0)!!.y.toDouble()

        return y1 + (((xi-x1) * (y2-y1)) / (x2-x1))

    }

    private fun linear_interpolate(a : Text.Element, b : Point, xi : Double) : Double {

        val x1 = a.cornerPoints?.get(0)!!.x.toDouble()
        val y1 = a.cornerPoints?.get(0)!!.y.toDouble()
        val x2 = b.x.toDouble()
        val y2 = b.y.toDouble()

        return y1 + (((xi-x1) * (y2-y1)) / (x2-x1))

    }

    private fun getAveragePoint(f: List<Text.Element>) : Point {
        val len = f.size
        var x = 0.0;
        var y = 0.0;

        f.forEach { text ->
            x += text.cornerPoints?.get(0)!!.x.toDouble()
            y += text.cornerPoints?.get(0)!!.y.toDouble()
        }

        x /= len;
        y /= len;

        return Point(x.toInt(), y.toInt())
    }
    private fun googleGetReceiptBlocks(text : Text) : List<Text.TextBlock>  {
        var receiptLines = mutableListOf<Text.TextBlock>()
        val blocks = text.textBlocks
        for (block in blocks) {
            val blockText = block.text
            val blockCornerPoints = block.cornerPoints
            val blockFrame = block.boundingBox
            receiptLines.add(block)
        }
        return receiptLines
    }

    private fun printOriginalReceiptBlocks(blocks : List<Text.TextBlock>) {
        blocks.forEach {
            Log.i("block", it.text)
        }
    }

    private fun printReceiptBlocks(blocks : List<List<Text.Element>>) {
        blocks.forEach {
            var text = ""
            it.forEach {
                text += it.text + " "
            }
            Log.i("line",text)
        }
    }

    private fun printReceiptProducts(products : List<Pair<String, Pair<Double, String>>>) {
        products.forEach {
            Log.i("product", "name: ${it.first}, unit: ${it.second.second}, amount: ${it.second.first}")
        }
    }

    private fun printAccuracy() {
        Log.i("accuracy increase", ((diff / total) * 100).toString())
    }
}