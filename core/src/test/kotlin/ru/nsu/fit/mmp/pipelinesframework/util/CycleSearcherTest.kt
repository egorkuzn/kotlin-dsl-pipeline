package ru.nsu.fit.mmp.pipelinesframework.util

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import ru.nsu.fit.mmp.pipelinesframework.node.Input1
import ru.nsu.fit.mmp.pipelinesframework.node.Input1Output1
import ru.nsu.fit.mmp.pipelinesframework.node.Node
import ru.nsu.fit.mmp.pipelinesframework.node.Output1
import ru.nsu.fit.mmp.pipelinesframework.pipe.Pipe

class CycleSearcherTest : StringSpec({
    "Из вершины А можно добраться в вершину Б" {
        val pipeFromAToB = Pipe<Any>()
        val a = Output1(
            "A",
            pipeFromAToB
        ) { }
        val b = Input1(
            "B",
            pipeFromAToB
        ) { }
        val expected = true

        val actual = CycleSearcherUtil().isConnected(a, b)

        actual shouldBe expected
    }

    "Из вершины А нельзя добраться в вершину Б" {
        val pipeFromAToOther = Pipe<Any>()
        val pipeToBFromOther = Pipe<Any>()
        val a = Output1(
            "A",
            pipeFromAToOther,
        ) {
        }
        val b = Input1(
            "B",
            pipeToBFromOther
        ) {
        }
        val expected = false

        val actual = CycleSearcherUtil().isConnected(a, b)

        actual shouldBe expected
    }

    "Существует цикл | Текущая вершина ведёт в начальную" {
        val pipeFromAToB = Pipe<Any>()
        val pipeFromBToA = Pipe<Any>()
        val a = Input1Output1(
            "A",
            pipeFromBToA,
            pipeFromAToB,
        )
        { _, _ ->
        }

        val b = Input1Output1(
            "B",
            pipeFromAToB,
            pipeFromBToA
        )
        { _, _ ->
        }

        CycleSearcherUtil().getCycleChain(a, emptyList(), b).isNotEmpty() shouldBe true
        CycleSearcherUtil().isContainsCycle(listOf(a, b)) shouldBe true
    }

    "Существует цикл | A -> B -> C" {
        val pipeFromAToB = Pipe<Any>()
        val pipeFromBToC = Pipe<Any>()
        val pipeFromCToA = Pipe<Any>()
        val a = Input1Output1(
            "A",
            pipeFromCToA,
            pipeFromAToB,
        ) { _, _ ->
        }
        val b = Input1Output1(
            "B",
            pipeFromAToB,
            pipeFromBToC,
        )
        { _, _ ->
        }

        val c = Input1Output1(
            name = "C",
            input1 = pipeFromBToC,
            output1 = pipeFromCToA
        )
        { _, _ ->
        }

        CycleSearcherUtil().getCycleChain(a, listOf(b), c).isNotEmpty() shouldBe true
        CycleSearcherUtil().isContainsCycle(listOf(a, b, c)) shouldBe true
    }
})