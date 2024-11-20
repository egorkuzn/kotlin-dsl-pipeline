package ru.nsu.fit.mmp.`24221`

fun main(args: Array<String>) {
	println("Hello World!")
}
//
//class Pipe<T>
//class Node(name: String);
//class Workflow(val nodes: List<Node>)
//
//class Producer<T>(){
//		fun onListener(action: ()->Unit){
//
//		}
//}
//class Consumer<T>(){
//		fun commit(value: T){
//
//		}
//}
//
//class WorkflowBuilder {
//		private val nodes = mutableListOf<Node>()
//
//		fun <T, Q> node(name: String, input: Pipe<T>, output: Pipe<Q>) {
//				nodes.add(Node(name))
//		}
//
//		fun <T, Q, S> node(name: String, input: Pair<Pipe<T>, Pipe<Q>>, output: Pipe<S>) {
//				nodes.add(Node(name))
//		}
//
//		fun <T, Q, S, P> node(
//			name: String,
//			input: Pair<Pipe<T>, Pipe<Q>>,
//			output: Pair<Pipe<S>, Pipe<P>>,
//			action: (Producer<T>, Producer<Q>, Consumer<S>, Consumer<P>) -> Unit
//		) {
//				nodes.add(Node(name))
//		}
//
//		fun build(): Workflow {
//				return Workflow(nodes)
//		}
//}
//
//fun Workflow(init: WorkflowBuilder.() -> Unit): Workflow {
//		return WorkflowBuilder().apply(init).build()
//}
//
//
// inline fun <T,Q,W> sharedWorkflow (input: Pipe<T>, output: Pipe<Q>, firstParam : W): Workflow {}
//
//
//
//val sharedFlow = sharedWorkflow(firstParam:Int, secondParam){
//		input: Pair<Int, Int>, output: Pair<Int, Int> ->
//
//
//}
//
//val workflow = Workflow {
//
//		val ip = Pipe<Int>()
//		val an = Pipe<Float>()
//		val gk = Pipe<Double>()
//
//		node(
//				name = "Первая нода",
//				input = Pair(ip, an),
//				output = Pair(ip, gk)
//		) { a: Producer<Int>, b: Producer<Float>, c: Consumer<Int>, d: Consumer<Double> ->
//				// Define the action logic here
//				a.onListener {  }
//				println("Executing action for node 'Первая нода'")
//				// e.g., Use producers and consumers as needed
//		}
//}
