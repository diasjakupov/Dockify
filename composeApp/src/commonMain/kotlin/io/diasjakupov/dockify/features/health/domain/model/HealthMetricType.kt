package io.diasjakupov.dockify.features.health.domain.model

/**
 * Enum representing the types of health metrics supported.
 */
enum class HealthMetricType(val displayName: String, val defaultUnit: String) {
    STEPS("Steps", "steps"),
    HEART_RATE("Heart Rate", "bpm"),
    BLOOD_PRESSURE_SYSTOLIC("Systolic Blood Pressure", "mmHg"),
    BLOOD_PRESSURE_DIASTOLIC("Diastolic Blood Pressure", "mmHg"),
    BLOOD_OXYGEN("Blood Oxygen", "%"),
    SLEEP_DURATION("Sleep Duration", "hours"),
    CALORIES_BURNED("Calories Burned", "kcal"),
    DISTANCE("Distance", "km"),
    WEIGHT("Weight", "kg"),
    HEIGHT("Height", "cm"),
    BODY_TEMPERATURE("Body Temperature", "°C"),
    RESPIRATORY_RATE("Respiratory Rate", "breaths/min");

    companion object {
        /**
         * Converts a string metric type to HealthMetricType enum.
         * Returns null if the type is not recognized.
         */
        fun fromString(value: String): HealthMetricType? {
            return entries.find {
                it.name.equals(value, ignoreCase = true) ||
                it.displayName.equals(value, ignoreCase = true)
            }
        }
    }
}
