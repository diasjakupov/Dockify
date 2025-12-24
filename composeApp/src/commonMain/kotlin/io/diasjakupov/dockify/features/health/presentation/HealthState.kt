package io.diasjakupov.dockify.features.health.presentation

import io.diasjakupov.dockify.features.health.domain.model.HealthMetric
import io.diasjakupov.dockify.features.health.domain.model.HealthMetricType
import io.diasjakupov.dockify.features.health.presentation.components.ActivityProgress
import io.diasjakupov.dockify.features.health.presentation.components.TodaySummary
import io.diasjakupov.dockify.features.health.presentation.components.getGreetingForTime
import io.diasjakupov.dockify.features.health.presentation.components.getMotivationalMessage
import io.diasjakupov.dockify.features.recommendation.domain.model.Recommendation
import io.diasjakupov.dockify.ui.base.LoadingState
import io.diasjakupov.dockify.ui.base.UiState
import io.diasjakupov.dockify.ui.base.WithError
import io.diasjakupov.dockify.ui.base.WithLoading

/**
 * Permission state for health data access.
 */
enum class PermissionState {
    Unknown,
    Granted,
    Denied,
    NotAvailable
}

/**
 * UI state for the Health screen.
 * Enhanced to support the new dashboard design with activity rings, vitals, and summaries.
 */
data class HealthState(
    val healthMetrics: List<HealthMetric> = emptyList(),
    val recommendation: Recommendation? = null,
    val isRecommendationLoading: Boolean = false,
    val permissionState: PermissionState = PermissionState.Unknown,
    val isPlatformHealthAvailable: Boolean = true,
    val isSyncing: Boolean = false,
    val lastSyncTimestamp: Long? = null,
    val streakDays: Int = 0,
    val stepsGoal: Int = 10000,
    val caloriesGoal: Int = 500,
    val distanceGoal: Double = 5.0,
    val currentHour: Int = 12,
    // Background sync state for optimistic updates
    val isBackgroundSyncing: Boolean = false,
    val backgroundSyncError: String? = null,
    val hasInitiallyLoadedPlatformData: Boolean = false,
    override val loadingState: LoadingState = LoadingState.IDLE,
    override val error: String? = null
) : UiState, WithLoading, WithError {

    val hasMetrics: Boolean
        get() = healthMetrics.isNotEmpty()

    val canSync: Boolean
        get() = !isSyncing && !isLoading && permissionState == PermissionState.Granted

    val needsPermission: Boolean
        get() = permissionState == PermissionState.Unknown ||
                permissionState == PermissionState.Denied

    val showBackgroundSyncIndicator: Boolean
        get() = isBackgroundSyncing || backgroundSyncError != null

    /**
     * Activity progress for the activity ring display.
     */
    val activityProgress: ActivityProgress
        get() {
            val steps = healthMetrics.find { it.type == HealthMetricType.STEPS }?.value?.toInt() ?: 0
            val calories = healthMetrics.find { it.type == HealthMetricType.CALORIES_BURNED }?.value?.toInt() ?: 0
            val distance = healthMetrics.find { it.type == HealthMetricType.DISTANCE }?.value ?: 0.0

            return ActivityProgress(
                steps = steps,
                stepsGoal = stepsGoal,
                calories = calories,
                caloriesGoal = caloriesGoal,
                distance = distance,
                distanceGoal = distanceGoal
            )
        }

    /**
     * Today's summary for the summary card.
     */
    val todaySummary: TodaySummary
        get() {
            val goalsCompleted = calculateGoalsCompleted()
            val totalGoals = 3

            return TodaySummary(
                greeting = getGreetingForTime(currentHour),
                streakDays = streakDays,
                goalsCompleted = goalsCompleted,
                totalGoals = totalGoals,
                motivationalMessage = getMotivationalMessage(goalsCompleted, totalGoals, streakDays),
                todayHighlight = getTodayHighlight()
            )
        }

    /**
     * Vital signs metrics (heart rate, blood oxygen, sleep, blood pressure, etc.)
     */
    val vitalMetrics: List<HealthMetric>
        get() = healthMetrics.filter { metric ->
            metric.type in listOf(
                HealthMetricType.HEART_RATE,
                HealthMetricType.BLOOD_OXYGEN,
                HealthMetricType.SLEEP_DURATION,
                HealthMetricType.BLOOD_PRESSURE_SYSTOLIC,
                HealthMetricType.WEIGHT,
                HealthMetricType.BODY_TEMPERATURE
            )
        }

    /**
     * Activity metrics (steps, calories, distance)
     */
    val activityMetrics: List<HealthMetric>
        get() = healthMetrics.filter { metric ->
            metric.type in listOf(
                HealthMetricType.STEPS,
                HealthMetricType.CALORIES_BURNED,
                HealthMetricType.DISTANCE
            )
        }

    private fun calculateGoalsCompleted(): Int {
        var completed = 0
        val steps = healthMetrics.find { it.type == HealthMetricType.STEPS }?.value?.toInt() ?: 0
        val calories = healthMetrics.find { it.type == HealthMetricType.CALORIES_BURNED }?.value?.toInt() ?: 0
        val distance = healthMetrics.find { it.type == HealthMetricType.DISTANCE }?.value ?: 0.0

        if (steps >= stepsGoal) completed++
        if (calories >= caloriesGoal) completed++
        if (distance >= distanceGoal) completed++

        return completed
    }

    private fun getTodayHighlight(): String? {
        val steps = healthMetrics.find { it.type == HealthMetricType.STEPS }?.value?.toInt() ?: 0
        val calories = healthMetrics.find { it.type == HealthMetricType.CALORIES_BURNED }?.value?.toInt() ?: 0

        return when {
            steps >= stepsGoal * 1.5 -> "Exceptional step count today!"
            calories >= caloriesGoal * 1.2 -> "Great calorie burn today!"
            streakDays >= 7 -> "Week-long streak achieved!"
            else -> null
        }
    }
}
