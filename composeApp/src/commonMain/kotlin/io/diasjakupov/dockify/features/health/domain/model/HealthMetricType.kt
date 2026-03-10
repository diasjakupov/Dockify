package io.diasjakupov.dockify.features.health.domain.model

/**
 * Health metric types supported by the platform and backend.
 *
 * [backendKey] is the exact string sent to the backend API.
 * It may differ from the Kotlin enum name (e.g. CALORIES_BURNED → "CALORIES").
 * Note: `name` is not accessible as a default in enum constructors,
 * so every entry must supply backendKey explicitly.
 */
enum class HealthMetricType(
    val displayName: String,
    val defaultUnit: String,
    val backendKey: String
) {
    // ── Existing platform-read metrics ──────────────────────────────────
    STEPS("Steps", "steps", "STEPS"),
    HEART_RATE("Heart Rate", "bpm", "HEART_RATE"),
    BLOOD_PRESSURE_SYSTOLIC("Systolic BP", "mmHg", "BLOOD_PRESSURE_SYSTOLIC"),
    BLOOD_PRESSURE_DIASTOLIC("Diastolic BP", "mmHg", "BLOOD_PRESSURE_DIASTOLIC"),
    BLOOD_OXYGEN("Blood Oxygen", "%", "BLOOD_OXYGEN"),
    SLEEP_DURATION("Sleep Duration", "hours", "SLEEP_DURATION"),
    CALORIES_BURNED("Calories Burned", "kcal", "CALORIES"),
    DISTANCE("Distance", "km", "DISTANCE"),
    WEIGHT("Weight", "kg", "WEIGHT"),
    HEIGHT("Height", "cm", "HEIGHT_M"),
    BODY_TEMPERATURE("Body Temperature", "°C", "BODY_TEMPERATURE"),
    RESPIRATORY_RATE("Respiratory Rate", "breaths/min", "RESPIRATORY_RATE"),

    // ── Missing types required by backend ML model ───────────────────────
    AGE("Age", "years", "AGE"),
    BMI("BMI", "kg/m²", "BMI"),
    FAT_PERCENTAGE("Body Fat", "%", "FAT_PERCENTAGE"),
    MAX_BPM("Max Heart Rate", "bpm", "MAX_BPM"),
    AVG_BPM("Avg Heart Rate", "bpm", "AVG_BPM"),
    RESTING_BPM("Resting Heart Rate", "bpm", "resting_bpm"),
    SESSION_DURATION_HOURS("Session Duration", "hours", "SESSION_DURATION_HOURS"),
    WORKOUT_FREQUENCY("Workout Frequency", "days/week", "WORKOUT_FREQUENCY"),
    DAILY_CALORIES("Daily Calorie Intake", "kcal", "DAILY_CALORIES"),
    WATER_INTAKE_LITERS("Water Intake", "L", "WATER_INTAKE_LITERS"),
    SLEEP_EFFICIENCY("Sleep Efficiency", "%", "SLEEP_EFFICIENCY"),
    TIME_IN_BED_HOURS("Time in Bed", "hours", "TIME_IN_BED_HOURS"),
    MOVEMENTS_PER_HOUR("Movements/Hour", "count", "MOVEMENTS_PER_HOUR"),
    SNORE_TIME("Snore Time", "min", "SNORE_TIME"),
    DAY_OF_WEEK("Day of Week", "", "DAY_OF_WEEK"),
    HOUR_STARTED("Hour Started", "", "HOUR_STARTED"),
    NOTE_COFFEE("Coffee", "flag", "NOTE_COFFEE"),
    NOTE_TEA("Tea", "flag", "NOTE_TEA"),
    NOTE_WORKOUT("Workout Note", "flag", "NOTE_WORKOUT"),
    NOTE_STRESS("Stress Note", "flag", "NOTE_STRESS"),
    NOTE_ATE_LATE("Ate Late", "flag", "NOTE_ATE_LATE");

    companion object {
        fun fromString(value: String): HealthMetricType? =
            entries.find {
                it.name.equals(value, ignoreCase = true) ||
                it.displayName.equals(value, ignoreCase = true) ||
                it.backendKey.equals(value, ignoreCase = true)
            }
    }
}
