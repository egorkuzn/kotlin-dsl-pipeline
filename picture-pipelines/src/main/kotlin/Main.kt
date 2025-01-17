package ru.nsu.fit.mmp.pipelinesframework

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import ru.nsu.fit.mmp.pipelinesframework.pipe.Pipe
import ru.nsu.fit.mmp.pipelinesframework.workflow.SharedWorkflow
import ru.nsu.fit.mmp.pipelinesframework.workflow.Workflow
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.time.Duration.Companion.seconds


fun line() = println("-".repeat(30))

fun main() {
    line()
    val nodesCut = SharedWorkflow() {

    }
    val workflow = Workflow {

        val partImage = Pipe<BufferedImage>()
        val partImageChangeable = Pipe<BufferedImage>()

        val inputDir = File("") //добавить путь до папки
        val outputDir = File("") //добавить путь до папки

        val imageFiles = inputDir.listFiles { file ->
            file.extension in listOf("jpg", "jpeg", "png")
        }

        node(
            name = "Rotated180",
            inputs = partImage,
            outputs = partImageChangeable
        ) { consumer, producer ->
            consumer.onListener { producer.commit(EditImage.rotateByDegrees(it, 180.0)) }
        }

        node(
                name = "Rotated90",
                inputs = partImage,
                outputs = partImageChangeable
        ) { consumer, producer ->
            consumer.onListener { producer.commit(EditImage.rotateByDegrees(it, 90.0)) }
        }

        node(
                name = "Rotated270",
                inputs = partImage,
                outputs = partImageChangeable
        ) { consumer, producer ->
            consumer.onListener { producer.commit(EditImage.rotateByDegrees(it, 270.0)) }
        }

        node(
                name = "Grayscale",
                inputs = partImage,
                outputs = partImageChangeable
        ) { consumer, producer ->
            consumer.onListener { producer.commit(EditImage.convertGrayScale(it)) }
        }

        node(
                name = "Threshold method (with a given threshold)",
                inputs = partImage,
                outputs = partImageChangeable
        ) { consumer, producer ->
            consumer.onListener { producer.commit(EditImage.thresholdImage(it, 100)) }
        }

        node(
                name = "Threshold method",
                inputs = partImage,
                outputs = partImageChangeable
        ) { consumer, producer ->
            consumer.onListener { producer.commit(EditImage.thresholdImage(it)) }
        }



        initial(
            name = "Поток исходных изображений",
            output = partImage
        ) { producer ->
            for (file in imageFiles) {
                val image: BufferedImage = ImageIO.read(file)
                producer.commit(image.getSubimage(0, 0, image.width / 2, image.height))
            }
        }


        terminate(
            name = "Поток изменных изображений",
            input = partImageChangeable
        ) { consumer ->
            val img1 = consumer.receive()
            val img2 = consumer.receive()

            val width = img1.width + img2.width
            val height = maxOf(img1.height, img2.height)

            val mergedImage = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
            val g2d = mergedImage.createGraphics()

            g2d.drawImage(img1, 0, 0, null)
            g2d.drawImage(img2, img1.width, 0, null)

            g2d.dispose()
            ImageIO.write(mergedImage, "jpg", outputDir)
        }
    }

    workflow.start()

    runBlocking {
        delay(10.seconds)
        workflow.stop()
    }

    line()
}

