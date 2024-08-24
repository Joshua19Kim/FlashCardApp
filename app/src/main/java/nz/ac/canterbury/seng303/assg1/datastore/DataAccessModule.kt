package nz.ac.canterbury.seng303.assg1.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.FlowPreview
import nz.ac.canterbury.seng303.assg1.models.Card
import nz.ac.canterbury.seng303.assg1.viewmodels.CardViewModel
import nz.ac.canterbury.seng303.assg1.viewmodels.CreateCardViewModel
import nz.ac.canterbury.seng303.assg1.viewmodels.PlayCardViewModel
import nz.ac.canterbury.seng303.assg1.viewmodels.PlayerNameViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "nz.ac.canterbury.seng303.assg1.shared.preferences")

@FlowPreview
val dataAccessModule = module {
    single<Storage<Card>> {
        PersistentStorage(
            gson = get(),
            type = object: TypeToken<List<Card>>(){}.type,
            preferenceKey = stringPreferencesKey("cards"),
            dataStore = androidContext().dataStore
        )
    }

    single { Gson() }

    viewModel { CardViewModel(get()) }
    viewModel { CreateCardViewModel(get()) }
    viewModel { PlayCardViewModel(get(), get()) }
    viewModel { PlayerNameViewModel() }
//    factory { CreateCardViewModel.provideFactory(cardStorage = get()) }
}
