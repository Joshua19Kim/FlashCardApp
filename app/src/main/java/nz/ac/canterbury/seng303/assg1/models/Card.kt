package nz.ac.canterbury.seng303.assg1.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Card(
    val id: Int,
    val title: String,
    val options: List<Option>,
    val correctOptionId: Set<Int>
) : Parcelable, Identifiable {

    @Parcelize
    data class Option(
        val id: Int,
        val text: String
    ) : Parcelable

    companion object {
        fun create(id: Int, title: String, initialOptions: List<String>, correctOptionIndices: Set<Int>): Card {
            val options = initialOptions.mapIndexed { index, text -> Option(index, text) }
            return Card(id, title, options, correctOptionIndices)
        }
    }

    override fun getIdentifier(): Int {
        return id
    }
}