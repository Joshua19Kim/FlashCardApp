package nz.ac.canterbury.seng303.assg1.models


data class Card(
    val id: Int,
    val title: String,
    val options: List<Option>,
    val correctOptionId: Set<Int>
) : Identifiable {
    data class Option(
        val id: Int,
        val text: String
    )
    override fun getIdentifier(): Int = id

    companion object {
        fun create(id: Int, title: String, initialOptions: List<String>, correctOptionIndices: Set<Int>): Card {
            require(initialOptions.size >= 2) { "A flash card must have at least two options" }
            require(correctOptionIndices.all { it in initialOptions.indices}) { "Correct option index out of bounds" }

            val options = initialOptions.mapIndexed { index, text -> Option(index, text) }
            return Card(id, title, options, correctOptionIndices)
        }
    }
}