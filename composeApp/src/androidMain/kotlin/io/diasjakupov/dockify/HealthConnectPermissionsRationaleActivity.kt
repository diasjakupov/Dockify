package io.diasjakupov.dockify

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Activity that displays the privacy policy / permissions rationale for Health Connect.
 * This is required by Health Connect to explain why the app needs access to health data.
 *
 * This activity is launched when:
 * - User taps on the app in Health Connect's permission management screen
 * - Health Connect needs to show the app's data usage rationale
 */
class HealthConnectPermissionsRationaleActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Health Data Usage",
                            style = MaterialTheme.typography.headlineMedium
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "Dockify uses Health Connect to read your health and fitness data " +
                                    "to provide personalized health insights and recommendations.",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "We may read the following data types:\n" +
                                    "• Steps\n" +
                                    "• Heart Rate\n" +
                                    "• Blood Pressure\n" +
                                    "• Blood Oxygen\n" +
                                    "• Sleep Duration\n" +
                                    "• Calories Burned\n" +
                                    "• Distance\n" +
                                    "• Weight & Height\n" +
                                    "• Body Temperature\n" +
                                    "• Respiratory Rate",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Start
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Your health data is synced securely to our servers to provide " +
                                    "health recommendations and enable features like finding nearby " +
                                    "hospitals. We do not share your data with third parties.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        Button(onClick = { finish() }) {
                            Text("Close")
                        }
                    }
                }
            }
        }
    }
}
