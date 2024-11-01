import com.igo.fluxcard.BuildConfig
import com.igo.fluxcard.model.entity.ImageDetails
import com.igo.fluxcard.model.repository.ImageApiService
import com.squareup.moshi.Moshi
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class RepImage {
    private val moshi = Moshi.Builder().build() // Добавляем создание Moshi
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.unsplash.com/")
        .addConverterFactory(MoshiConverterFactory.create(moshi)) // Используем созданный moshi здесь
        .build()

    private val apiService = retrofit.create(ImageApiService::class.java)

    suspend fun getImage(query: String): ImageDetails? {
        val response = apiService.getImage(query, BuildConfig.SPLASH_API_KEY)
        return if (response.isSuccessful) {
            response.body()?.results?.firstOrNull()
        } else {
            null
        }
    }
}