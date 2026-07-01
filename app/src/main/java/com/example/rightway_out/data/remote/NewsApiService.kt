package com.example.rightway_out.data.remote

import retrofit2.http.GET

interface NewsApiService {
    @GET("announcements")
    suspend fun getAnnouncements(): List<AnnouncementDto>
}

data class AnnouncementDto(
    val id: Int = 0,
    val title: String = "",
    val body: String = "",
    val date: String = ""
)
