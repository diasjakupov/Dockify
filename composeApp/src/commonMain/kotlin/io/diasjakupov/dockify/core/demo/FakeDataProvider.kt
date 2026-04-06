package io.diasjakupov.dockify.core.demo

import io.diasjakupov.dockify.features.health.domain.model.HealthMetric
import io.diasjakupov.dockify.features.health.domain.model.HealthMetricType
import io.diasjakupov.dockify.features.location.domain.model.Hospital
import io.diasjakupov.dockify.features.location.domain.model.Location
import io.diasjakupov.dockify.features.location.domain.model.NearbyUser

object FakeDataProvider {

    val healthMetrics: List<HealthMetric> = listOf(
        HealthMetric(type = HealthMetricType.HEART_RATE, value = 72.0, unit = "bpm"),
        HealthMetric(type = HealthMetricType.BLOOD_OXYGEN, value = 98.0, unit = "%"),
        HealthMetric(type = HealthMetricType.SLEEP_DURATION, value = 7.5, unit = "hours"),
        HealthMetric(type = HealthMetricType.STEPS, value = 8432.0, unit = "steps"),
        HealthMetric(type = HealthMetricType.CALORIES_BURNED, value = 347.0, unit = "kcal"),
        HealthMetric(type = HealthMetricType.DISTANCE, value = 3.2, unit = "km"),
        HealthMetric(type = HealthMetricType.WEIGHT, value = 75.0, unit = "kg"),
        HealthMetric(type = HealthMetricType.BLOOD_PRESSURE_SYSTOLIC, value = 120.0, unit = "mmHg"),
        HealthMetric(type = HealthMetricType.BLOOD_PRESSURE_DIASTOLIC, value = 80.0, unit = "mmHg"),
        HealthMetric(type = HealthMetricType.BODY_TEMPERATURE, value = 36.6, unit = "°C"),
    )

    val nearbyUsers: List<NearbyUser> = listOf(
        NearbyUser(userId = "demo-user-1", location = Location(43.2380, 76.9458)),
        NearbyUser(userId = "demo-user-2", location = Location(43.2220, 76.9550)),
        NearbyUser(userId = "demo-user-3", location = Location(43.2150, 76.9270)),
        NearbyUser(userId = "demo-user-4", location = Location(43.2280, 76.8750)),
        NearbyUser(userId = "demo-user-5", location = Location(43.2050, 76.8420)),
        NearbyUser(userId = "demo-user-6", location = Location(43.2900, 76.9600)),
        NearbyUser(userId = "demo-user-7", location = Location(43.2650, 76.9380)),
        NearbyUser(userId = "demo-user-8", location = Location(43.1850, 76.8850)),
    )

    val nearbyHospitals: List<Hospital> = listOf(
        Hospital(location = Location(43.2380, 76.9150), name = "City Clinical Hospital #4"),
        Hospital(location = Location(43.2210, 76.9430), name = "Central Clinical Hospital"),
        Hospital(location = Location(43.2560, 76.9280), name = "Children's Clinical Hospital"),
        Hospital(location = Location(43.2470, 76.9520), name = "City Emergency Hospital"),
        Hospital(location = Location(43.2100, 76.8950), name = "Regional Diagnostic Center"),
    )
}
