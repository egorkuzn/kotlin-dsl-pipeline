import io.kotest.core.spec.style.FunSpec
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldHaveLength
import io.kotest.matchers.string.startWith

/**
 * More about tests specs you can find there: https://kotest.io/docs/framework/testing-styles.html
 */

/**
 * If we prefer string naming of tests
 */
class mainTestStringSpec : StringSpec({
    "length should return size of string" {
        "hello".length shouldBe 5
    }
    "startsWith should test for a prefix" {
        "world" should startWith("wor")
    }
})

/**
 * If we prefer simple way: test() function with a description as an argument
 */
class mainTestFunSpec : FunSpec({

    beforeEach {
        println("Hello from $it")
    }

    test("sam should be a three letter name") {
        "sam".shouldHaveLength(3)
    }

    test("axiomatic") {
        true shouldBe true
    }

    afterEach {
        println("Goodbye from $it")
    }
})
