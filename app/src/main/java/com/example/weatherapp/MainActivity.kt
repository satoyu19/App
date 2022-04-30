package com.example.weatherapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.example.weatherapp.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.URL

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    //api
    private val apiKey = "Your Key"
    private val mainUrl = "https://api.openweathermap.org/data/2.5/weather?lang=ja"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this,R.layout.activity_main)

        binding.btnTokyo.setOnClickListener{

            val weatherUrl = "$mainUrl&q=tokyo&appid=$apiKey"

            weatherTask(weatherUrl)
        }

        binding.btnOkinawa.setOnClickListener{

            val weatherUrl = "$mainUrl&q=okinawa&appid=$apiKey"

            weatherTask(weatherUrl)
        }

        binding.btnClear.setOnClickListener{
            binding.tvCity.text = "都市名"
            binding.tvWeather.text = "都市の天気"
            binding.tvMax.text = "最高気温"
            binding.tvMin.text = "最低気温"
        }

    }

    private fun weatherTask(weatherUrl: String) {

        //コルーチンスコープ(非同期処理の領域)を用意する
        lifecycleScope.launch {

            //http通信（ワーカースレッド）
            val result = weatherBackgroundTask(weatherUrl)

            //お天気データを表示
            weatherJsonTask(result)
        }
    }

    //ワーカースレッドで処理される、中断する可能性があるためsuspendを付与する
    private suspend fun weatherBackgroundTask(weatherUrl: String): String {

        //withContext=スレッドを分裂する、Dispatchers.IO=ワーカースレッド
        val responce = withContext(Dispatchers.IO){
            var httpResult = ""
            try{
                //ただの文字列をURL型に変換
                val url = URL(weatherUrl)

                //読み込んだデータを文字列にして代入
                //BufferedReader=テキストファイルを読み込むクラス、InputStreamReader=文字コードを読み込めるようにする準備
                val br = BufferedReader(InputStreamReader(url.openStream()))
                //読み込んだデータを文字列に変換すて代入、readText=URLかrテキストを直接読み込む拡張関数。
                httpResult = br.readText()
                Log.i("weatherApp", httpResult)

            }catch (e: IOException){
                e.printStackTrace()
            } catch (e: JSONException){
                e.printStackTrace()
            }
            return@withContext httpResult
        }
        return responce
    }

    //お天気データ（JSONデータ）を表示する(UIスレッド)
    private fun weatherJsonTask(result: String) {

        //JSONオブジェクト一式を生成
        val jsonObj = JSONObject(result)

        //JSONオブジェクトの都市名のキーを取得
        val cityName = jsonObj.getString("name")
        binding.tvCity.text = cityName

        //JSONオブジェクトの天気情報JSON配列を取得、そこからさらに配列の0番目から天気を取得
        val weatherJSONArray = jsonObj.getJSONArray("weather")
        val weatherJSON = weatherJSONArray.getJSONObject(0)
        val weather = weatherJSON.getString("description")
        binding.tvWeather.text = weather


        val main = jsonObj.getJSONObject("main")
        binding.tvMax.text = "最高気温：${main.getInt("temp_max") - 273}C"
        binding.tvMin.text = "最低気温：${main.getInt("temp_min") - 273}C"
    }

}