package com.example.simpleweatherapp

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.gson.GsonBuilder
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Weather()
        }
    }
}
data class WeatherResponse(
    val main: Main
)

data class Main(
    val temp: Float
)

interface RetrofitInterface {
    @GET("weather")
    suspend fun getWeather(
        @Query("q") city: String,
        @Query("appid") apiKey: String
    )
            : retrofit2.Response<WeatherResponse>
}

object ServiceBuilder {

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.openweathermap.org/data/2.5/")
        .addConverterFactory(GsonConverterFactory.create(GsonBuilder().setLenient().create()))
        .build()

    fun <T> buildService(service: Class<T>): T {
        return retrofit.create(service)
    }
}

suspend fun weather(city: String): Float {

    return try {

        val response = ServiceBuilder.buildService(RetrofitInterface::class.java)
            .getWeather(city, "729664ac11fd795189d8bbdd096156f3")

        if (response.isSuccessful && response.body() != null) {
            val temp = response.body()!!.main.temp
            val celciusTemp = temp - 273.15f
            celciusTemp
        } else {
            0f
        }
    } catch (e: Exception) {
        e.printStackTrace()
        0f
    }
}


@SuppressLint("DefaultLocale")
@Composable
fun Weather() {
    var fResult by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    val keyboard = LocalSoftwareKeyboardController.current
    val couroutineScope = rememberCoroutineScope()


    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {

        Column(
            modifier = Modifier
                .width(300.dp)
                .height(500.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically),
        ) {
            OutlinedTextField(
                value = city,
                onValueChange = { city = it },
                label = { Text("Enter City") })
            Button(onClick = {
                keyboard?.hide()
                couroutineScope.launch {
                    fResult = String.format("%.1f °C", weather(city))

                }


            }, enabled = city.isNotEmpty(), shape = RectangleShape) {
                Text(text = "Search")
            }
            OutlinedTextField(
                value = fResult,
                onValueChange = {},
                readOnly = true,
                textStyle = TextStyle(
                    Color.Black,
                    textAlign = TextAlign.Center,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                ),
                enabled = false,
                modifier = Modifier.wrapContentSize(),
                colors = OutlinedTextFieldDefaults.colors(
                    disabledBorderColor = Color.Transparent,
                )
            )

        }


    }

}

@Preview(showBackground = true)
@Composable
fun WeatherPreview() {
    Weather()
}
