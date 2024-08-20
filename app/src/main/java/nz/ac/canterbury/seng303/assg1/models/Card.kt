package nz.ac.canterbury.seng303.assg1.models

class Card (
    val id: Int,
    val title: String,
    val content: String,
    val timestamp: Long,
    val isArchived: Boolean): Identifiable {

    companion object {
        fun getCards(): List<Card> {
            return listOf(
                Card(
                    1,
                    "Meeting Agenda",
                    "Discuss project updates and future plans.",
                    1637653200000,
                    false
                ),
            )
        }
    }

    override fun getIdentifier(): Int {
        return id;
    }
}

