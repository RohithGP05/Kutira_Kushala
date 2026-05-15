package com.example.kutira_kushala

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.kutira_kushala.ui.theme.Kutira_KushalaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Kutira_KushalaTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    KutiraApp()
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun KutiraPreview() {
    Kutira_KushalaTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            KutiraApp()
        }
    }
}