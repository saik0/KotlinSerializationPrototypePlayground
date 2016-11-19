import kotlin.reflect.KFunction
import kotlin.serialization.KSerializable
import kotlin.serialization.KSerializer

// -------------------------------------------
// simple data objects

@KSerializable
data class CityData(
        val id: Int,
        val name: String
)

@KSerializable
data class StreetData(
        val id: Int,
        val name: String,
        val city: CityData
)

@KSerializable
data class StreetData2(
        val id: Int,
        val name: String,
        val city: CityData?
)

@KSerializable
data class CountyData(
        val name: String,
        val cities: List<CityData>
)


// -------------------------------------------
// testing framework

data class Result(
        val obj: Any, // original object
        val res: Any, // resulting object
        val ext: Any  // serialized (external) representation
)

data class Case<T: Any>(
        val serializer: KSerializer<T>,
        val obj: T,
        val name: String = obj.javaClass.simpleName
)

val testCases: List<Case<*>> = listOf(
        Case(CityData, CityData(1, "New York")),
        Case(StreetData, StreetData(2, "Broadway", CityData(1, "New York"))),
        Case(StreetData2, StreetData2(2, "Broadway", CityData(1, "New York"))),
        Case(StreetData2, StreetData2(2, "Broadway", null)),
        Case(CountyData, CountyData("US", listOf(CityData(1, "New York"), CityData(2, "Chicago")))),
        Case(Zoo, zoo),
        Case(Shop, shop)
)

@Suppress("UNCHECKED_CAST")
fun <T: Any> testCase(serializer: KSerializer<T>, obj: T, method: (KSerializer<Any>, Any) -> Result) {
    println("Start with $obj")
    val result = try { method(serializer as KSerializer<Any>, obj) } catch (e: Throwable) {
        println("Failed with $e")
        return
    }
    println("Loaded obj ${result.res}")
    println("    equals=${obj == result.res}, sameRef=${obj === result.res}")
    println("Saved form ${result.ext}")
}

fun testCase(case: Case<Any>, method: (KSerializer<Any>, Any) -> Result) {
    println("Test case ${case.name}")
    testCase(case.serializer, case.obj, method)
}

@Suppress("UNCHECKED_CAST")
fun testMethod(method: (KSerializer<Any>, Any) -> Result) {
    println("=== Running with ${(method as KFunction<*>).name} ===")
    testCases.forEach { case ->
        println()
        testCase(case as Case<Any>, method)
   }
}
