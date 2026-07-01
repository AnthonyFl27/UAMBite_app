package com.uambite.app.di

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.uambite.app.BuildConfig
import com.uambite.app.data.api.AuthApi
import com.uambite.app.data.api.DescuentosApi
import com.uambite.app.data.api.DetallesApi
import com.uambite.app.data.api.EntregasApi
import com.uambite.app.data.api.FranjasApi
import com.uambite.app.data.api.ImagenesApi
import com.uambite.app.data.api.IngredientesApi
import com.uambite.app.data.api.LocalesApi
import com.uambite.app.data.api.PagoApi
import com.uambite.app.data.api.PedidosApi
import com.uambite.app.data.api.ProductoIngredienteApi
import com.uambite.app.data.api.ProductosApi
import com.uambite.app.data.api.UsuariosApi
import com.uambite.app.data.auth.AuthInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, json: Json): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi = retrofit.create(AuthApi::class.java)

    @Provides
    @Singleton
    fun provideLocalesApi(retrofit: Retrofit): LocalesApi = retrofit.create(LocalesApi::class.java)

    @Provides
    @Singleton
    fun provideProductosApi(retrofit: Retrofit): ProductosApi =
        retrofit.create(ProductosApi::class.java)

    @Provides
    @Singleton
    fun provideIngredientesApi(retrofit: Retrofit): IngredientesApi =
        retrofit.create(IngredientesApi::class.java)

    @Provides
    @Singleton
    fun provideProductoIngredienteApi(retrofit: Retrofit): ProductoIngredienteApi =
        retrofit.create(ProductoIngredienteApi::class.java)

    @Provides
    @Singleton
    fun providePedidosApi(retrofit: Retrofit): PedidosApi =
        retrofit.create(PedidosApi::class.java)

    @Provides
    @Singleton
    fun provideDetallesApi(retrofit: Retrofit): DetallesApi =
        retrofit.create(DetallesApi::class.java)

    @Provides
    @Singleton
    fun providePagoApi(retrofit: Retrofit): PagoApi = retrofit.create(PagoApi::class.java)

    @Provides
    @Singleton
    fun provideDescuentosApi(retrofit: Retrofit): DescuentosApi =
        retrofit.create(DescuentosApi::class.java)

    @Provides
    @Singleton
    fun provideFranjasApi(retrofit: Retrofit): FranjasApi =
        retrofit.create(FranjasApi::class.java)

    @Provides
    @Singleton
    fun provideUsuariosApi(retrofit: Retrofit): UsuariosApi =
        retrofit.create(UsuariosApi::class.java)

    @Provides
    @Singleton
    fun provideEntregasApi(retrofit: Retrofit): EntregasApi =
        retrofit.create(EntregasApi::class.java)

    @Provides
    @Singleton
    fun provideImagenesApi(retrofit: Retrofit): ImagenesApi =
        retrofit.create(ImagenesApi::class.java)
}
