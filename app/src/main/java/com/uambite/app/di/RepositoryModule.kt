package com.uambite.app.di

import com.uambite.app.data.repository.AuthRepositoryImpl
import com.uambite.app.data.repository.CartRepositoryImpl
import com.uambite.app.data.repository.DescuentosRepositoryImpl
import com.uambite.app.data.repository.DetallesRepositoryImpl
import com.uambite.app.data.repository.EntregasRepositoryImpl
import com.uambite.app.data.repository.FranjasRepositoryImpl
import com.uambite.app.data.repository.ImagenesRepositoryImpl
import com.uambite.app.data.repository.IngredientesRepositoryImpl
import com.uambite.app.data.repository.LocalesRepositoryImpl
import com.uambite.app.data.repository.PagosRepositoryImpl
import com.uambite.app.data.repository.PedidosRepositoryImpl
import com.uambite.app.data.repository.ProductoIngredienteRepositoryImpl
import com.uambite.app.data.repository.ProductosRepositoryImpl
import com.uambite.app.data.repository.UsuariosRepositoryImpl
import com.uambite.app.domain.repository.AuthRepository
import com.uambite.app.domain.repository.CartRepository
import com.uambite.app.domain.repository.DescuentosRepository
import com.uambite.app.domain.repository.DetallesRepository
import com.uambite.app.domain.repository.EntregasRepository
import com.uambite.app.domain.repository.FranjasRepository
import com.uambite.app.domain.repository.ImagenesRepository
import com.uambite.app.domain.repository.IngredientesRepository
import com.uambite.app.domain.repository.LocalesRepository
import com.uambite.app.domain.repository.PagosRepository
import com.uambite.app.domain.repository.PedidosRepository
import com.uambite.app.domain.repository.ProductoIngredienteRepository
import com.uambite.app.domain.repository.ProductosRepository
import com.uambite.app.domain.repository.UsuariosRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindLocalesRepository(impl: LocalesRepositoryImpl): LocalesRepository

    @Binds
    @Singleton
    abstract fun bindProductosRepository(impl: ProductosRepositoryImpl): ProductosRepository

    @Binds
    @Singleton
    abstract fun bindIngredientesRepository(impl: IngredientesRepositoryImpl): IngredientesRepository

    @Binds
    @Singleton
    abstract fun bindProductoIngredienteRepository(
        impl: ProductoIngredienteRepositoryImpl
    ): ProductoIngredienteRepository

    @Binds
    @Singleton
    abstract fun bindCartRepository(impl: CartRepositoryImpl): CartRepository

    @Binds
    @Singleton
    abstract fun bindPedidosRepository(impl: PedidosRepositoryImpl): PedidosRepository

    @Binds
    @Singleton
    abstract fun bindDetallesRepository(impl: DetallesRepositoryImpl): DetallesRepository

    @Binds
    @Singleton
    abstract fun bindPagosRepository(impl: PagosRepositoryImpl): PagosRepository

    @Binds
    @Singleton
    abstract fun bindDescuentosRepository(impl: DescuentosRepositoryImpl): DescuentosRepository

    @Binds
    @Singleton
    abstract fun bindFranjasRepository(impl: FranjasRepositoryImpl): FranjasRepository

    @Binds
    @Singleton
    abstract fun bindUsuariosRepository(impl: UsuariosRepositoryImpl): UsuariosRepository

    @Binds
    @Singleton
    abstract fun bindEntregasRepository(impl: EntregasRepositoryImpl): EntregasRepository

    @Binds
    @Singleton
    abstract fun bindImagenesRepository(impl: ImagenesRepositoryImpl): ImagenesRepository
}
