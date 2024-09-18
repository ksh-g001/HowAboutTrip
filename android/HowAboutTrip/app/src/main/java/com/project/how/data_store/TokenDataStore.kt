package com.project.how.data_store

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.project.how.data_class.Tokens
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

object TokenDataStore {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "tokens")

    private val ACCESS_TOKEN = stringPreferencesKey("access_token")
    private val REFRESH_TOKEN = stringPreferencesKey("refresh_token")

    suspend fun saveTokens(context: Context, tokens : Tokens){
        context.dataStore.edit {
            it[ACCESS_TOKEN] = tokens.accessToken
            it[REFRESH_TOKEN] = tokens.refreshToken
        }
    }

    suspend fun clearTokens(context: Context){
        context.dataStore.edit {
            it.remove(ACCESS_TOKEN)
            it.remove(REFRESH_TOKEN)
        }
    }

    fun getTokens(context: Context): Flow<Tokens?> = flow {
        context.dataStore.data.collect {
            val accessToken = it[ACCESS_TOKEN]
            val refreshToken = it[REFRESH_TOKEN]
            if(accessToken.isNullOrEmpty() || refreshToken.isNullOrEmpty()){
                this.emit(null)
            }else{
                this.emit(Tokens(accessToken, refreshToken))
            }
        }
    }
}