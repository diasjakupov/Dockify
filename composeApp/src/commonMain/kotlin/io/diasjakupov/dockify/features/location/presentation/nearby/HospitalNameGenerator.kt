package io.diasjakupov.dockify.features.location.presentation.nearby

private val HOSPITAL_NAMES = listOf(
    "City Hospital",
    "Central Clinic",
    "Medical Center",
    "Emergency Care",
    "Regional Hospital"
)

fun generateHospitalName(index: Int): String {
    val name = HOSPITAL_NAMES[index % HOSPITAL_NAMES.size]
    val suffix = if (index >= HOSPITAL_NAMES.size) " #${index + 1}" else ""
    return "$name$suffix"
}
