package io.diasjakupov.dockify.features.recommendation.di

import io.diasjakupov.dockify.features.recommendation.data.datasource.RecommendationLocalDataSource
import io.diasjakupov.dockify.features.recommendation.data.datasource.RecommendationLocalDataSourceImpl
import io.diasjakupov.dockify.features.recommendation.data.datasource.RecommendationRemoteDataSource
import io.diasjakupov.dockify.features.recommendation.data.datasource.RecommendationRemoteDataSourceImpl
import io.diasjakupov.dockify.features.recommendation.data.repository.RecommendationRepositoryImpl
import io.diasjakupov.dockify.features.recommendation.domain.repository.RecommendationRepository
import io.diasjakupov.dockify.features.recommendation.domain.usecase.GetRecommendationUseCase
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * Koin module for recommendation feature dependencies.
 */
val recommendationModule: Module = module {
    single<RecommendationLocalDataSource> {
        RecommendationLocalDataSourceImpl()
    }

    single<RecommendationRemoteDataSource> {
        RecommendationRemoteDataSourceImpl(
            httpClient = get(),
            baseUrl = get(named("baseUrl"))
        )
    }

    single<RecommendationRepository> {
        RecommendationRepositoryImpl(
            remoteDataSource = get(),
            localDataSource = get()
        )
    }

    factory {
        GetRecommendationUseCase(
            recommendationRepository = get()
        )
    }
}
