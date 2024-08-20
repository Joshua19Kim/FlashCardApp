package nz.ac.canterbury.seng303.assg1.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import nz.ac.canterbury.seng303.assg1.models.Identifiable
import java.lang.reflect.Type

class PersistentStorage<T>(
    private val gson: Gson,
    private val type: Type,
    private val dataStore: DataStore<Preferences>,
    private val preferenceKey: Preferences.Key<String>
) : Storage<T> where T: Identifiable {

    override fun insert(data: T): Flow<Int> {
        return flow {
            val cachedDataClone = getAll().first().toMutableList()
            cachedDataClone.add(data)
            dataStore.edit {
                val jsonString = gson.toJson(cachedDataClone, type)
                it[preferenceKey] = jsonString
                emit(OPERATION_SUCCESS)
            }
        }
    }

    override fun insertAll(data: List<T>): Flow<Int> {
        return flow {
            val cachedDataClone = getAll().first().toMutableList()
            cachedDataClone.addAll(data)
            dataStore.edit {
                val jsonString = gson.toJson(cachedDataClone, type)
                it[preferenceKey] = jsonString
                emit(OPERATION_SUCCESS)
            }
        }
    }

    override fun getAll(): Flow<List<T>> {
        return dataStore.data.map { preferences ->
            val jsonString = preferences[preferenceKey] ?: EMPTY_JSON_STRING
            val elements = gson.fromJson<List<T>>(jsonString, type)
            elements
        }
    }

    override fun edit(identifier: Int, data: T): Flow<Int> {
        return flow {
            val cachedDataClone = getAll().first().toMutableList()
            val index = cachedDataClone.indexOfFirst { it.getIdentifier() == identifier }
            if (index != -1) {
                cachedDataClone[index] = data  // Update the item with the new data
                dataStore.edit {  // Save the updated list
                    val jsonString = gson.toJson(cachedDataClone, type)
                    it[preferenceKey] = jsonString
                    emit(OPERATION_SUCCESS)
                }
            } else {
                emit(OPERATION_FAILURE)  // Handle the case when the item with the given identifier is not found
            }
        }
    }

    override fun get(where: (T) -> Boolean): Flow<T> {
        return flow {
            val data = getAll().first().first(where)
            emit(data)
        }
    }

    override fun delete(identifier: Int): Flow<Int> {
        return flow {
            val cachedDataClone = getAll().first().toMutableList()
            val updatedData = cachedDataClone.filterNot { it.getIdentifier() == identifier }
            dataStore.edit {
                val jsonString = gson.toJson(updatedData, type)
                it[preferenceKey] = jsonString
                emit(OPERATION_SUCCESS)
            }
        }
    }

    companion object {
        private const val OPERATION_SUCCESS = 1
        private const val OPERATION_FAILURE = -1
        private const val EMPTY_JSON_STRING = "[]"
    }
}
