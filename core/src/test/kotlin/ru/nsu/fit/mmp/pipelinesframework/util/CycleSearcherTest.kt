package ru.nsu.fit.mmp.pipelinesframework.util

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import ru.nsu.fit.mmp.pipelinesframework.Node
import ru.nsu.fit.mmp.pipelinesframework.pipe.Pipe

class CycleSearcherTest: StringSpec({
    "Из вершины А можно добраться в вершину Б" {
        val pipeFromAToB = Pipe<Any>()
        val a = Node(
            name = "A",
            input = emptyList(),
            output = listOf(pipeFromAToB),
            actions = { },
        )
        val b = Node(
            name = "B",
            input = listOf(pipeFromAToB),
            output = emptyList(),
            actions = { }
        )
        val expected = true

        val actual = CycleSearcherUtil.isConnected(a, b)

        actual shouldBe expected
    }

    "Из вершины А нельзя добраться в вершину Б" {
        val pipeFromAToOther = Pipe<Any>()
        val pipeToBFromOther = Pipe<Any>()
        val a = Node(
            name = "A",
            input = emptyList(),
            output = listOf(pipeFromAToOther),
            actions = { },
        )
        val b = Node(
            name = "B",
            input = listOf(pipeToBFromOther),
            output = emptyList(),
            actions = { }
        )
        val expected = false

        val actual = CycleSearcherUtil.isConnected(a, b)

        actual shouldBe expected
    }

    "Существует цикл | Текущая вершина ведёт в начальную" {
        val pipeFromAToB = Pipe<Any>()
        val pipeFromBToA = Pipe<Any>()
        val a = Node(
            name = "A",
            input = listOf(pipeFromBToA),
            output = listOf(pipeFromAToB),
            actions = { },
        )
        val b = Node(
            name = "B",
            input = listOf(pipeFromAToB),
            output = listOf(pipeFromBToA),
            actions = { }
        )

        CycleSearcherUtil.isContainsCycle(a, emptyList(), b) shouldBe true
        CycleSearcherUtil.isContainsCycle(listOf(a, b)) shouldBe true
    }

    "Существует цикл | A -> B -> C" {
        val pipeFromAToB = Pipe<Any>()
        val pipeFromBToC = Pipe<Any>()
        val pipeFromCToA = Pipe<Any>()
        val a = Node(
            name = "A",
            input = listOf(pipeFromCToA),
            output = listOf(pipeFromAToB),
            actions = { },
        )
        val b = Node(
            name = "B",
            input = listOf(pipeFromAToB),
            output = listOf(pipeFromBToC),
            actions = { }
        )
        val c = Node(
            name = "C",
            input = listOf(pipeFromBToC),
            output = listOf(pipeFromCToA),
            actions = { }
        )

        CycleSearcherUtil.isContainsCycle(a, listOf(b), c) shouldBe true
        CycleSearcherUtil.isContainsCycle(listOf(a, b, c)) shouldBe true
    }
})