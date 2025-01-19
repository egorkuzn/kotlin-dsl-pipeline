package ru.nsu.fit.mmp.pipelinesframework

import EditImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import ru.nsu.fit.mmp.pipelinesframework.pipe.Pipe
import ru.nsu.fit.mmp.pipelinesframework.workflow.builder.Workflow
import ru.nsu.fit.mmp.pipelinesframework.workflow.builder.node
import ru.nsu.fit.mmp.pipelinesframework.workflow.builder.terminate
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import kotlin.time.Duration.Companion.seconds
import java.io.File
import java.util.UUID


fun line() = println("-".repeat(30))

fun main() {
    line()
    val workflow = Workflow {

        val partImage = Pipe<BufferedImage>()
        val partImageChangeable1 = Pipe<BufferedImage>()
        val partImageChangeable2 = Pipe<BufferedImage>()

        val inputDir = File(this::class.java.getResource("/in")?.path!!)
        // SEE IN BUILD RESOURCES
        val outputDirPath = this::class.java.getResource("/out")?.path!!

        val imageFiles = inputDir.listFiles { file ->
            file.extension in listOf("jpg", "jpeg", "png")
        }

        node(name = "Rotated180", input = partImage, output = partImageChangeable1){ consumer, producer ->
            consumer.onListener {
                producer.commit(EditImage.rotateByDegrees(it, 180.0))
            }
        }

        node(
                name = "Rotated90",
                input = partImage,
                output = partImageChangeable1
        ) { consumer, producer ->
            consumer.onListener { producer.commit(EditImage.rotateByDegrees(it, 90.0)) }
        }

        node(
                name = "Rotated270",
                input = partImage,
                output = partImageChangeable2
        ) { consumer, producer ->
            consumer.onListener {
                producer.commit(EditImage.rotateByDegrees(it, 270.0))
            }
        }

        node(
                name = "Grayscale",
                input = partImage,
                output = partImageChangeable2
        ) { consumer, producer ->
            consumer.onListener {
                producer.commit(EditImage.convertGrayScale(it))
            }
        }

        node(
                name = "Threshold method (with a given threshold)",
                input = partImage,
                output = partImageChangeable1
        ) { consumer, producer ->
            consumer.onListener {
                producer.commit(EditImage.thresholdImage(it, 100))
            }
        }

        node(
                name = "Threshold method",
                input = partImage,
                output = partImageChangeable1
        ) { consumer, producer ->
            consumer.onListener {
                producer.commit(EditImage.thresholdImage(it))
            }
        }



        initial(
            name = "Поток исходных изображений",
            output = partImage
        ) { producer ->
            for (file in imageFiles!!) {
                println(file.name)
                val image: BufferedImage = ImageIO.read(file)
                producer.commit(image.getSubimage(0, 0, image.width / 2, image.height))
            }
        }

        terminate(
            name = "Поток изменных изображений",
            inputs = Pair(partImageChangeable1, partImageChangeable2)
        ) { consumer1, consumer2 ->

            (consumer1+consumer2).onListener{
                img1, img2 ->

                val width = img1.width + img2.width
                val height = maxOf(img1.height, img2.height)

                val mergedImage = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
                val g2d = mergedImage.createGraphics()

                g2d.drawImage(img1, 0, 0, null)
                g2d.drawImage(img2, img1.width, 0, null)

                g2d.dispose()
                val file = File("$outputDirPath/${UUID.randomUUID()}.jpg")
                ImageIO.write(mergedImage, "jpg", file)
            }
        }
    }

    workflow.start()

    runBlocking {
        delay(30.seconds)
        workflow.stop()
    }

    line()
}

