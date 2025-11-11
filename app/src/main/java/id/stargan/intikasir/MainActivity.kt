package id.stargan.intikasir

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import id.stargan.intikasir.ui.screen.pos.PosScreen
import id.stargan.intikasir.ui.theme.IntiKasirTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            IntiKasirTheme {
                PosScreen(
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
