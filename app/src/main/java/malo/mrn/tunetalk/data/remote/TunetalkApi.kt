package malo.mrn.tunetalk.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface TunetalkApi {
    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequestDto): AuthResponseDto

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequestDto): AuthResponseDto

    @GET("api/channels/genres")
    suspend fun getGenres(): List<GenreDto>

    @GET("api/channels")
    suspend fun getChannels(@Query("genre") genreId: String): List<RadioChannelDto>

    @GET("api/channels/{channelId}/messages")
    suspend fun getRecentMessages(
        @Path("channelId") channelId: String,
        @Header("Authorization") authorization: String
    ): List<ChatMessageDto>
}
