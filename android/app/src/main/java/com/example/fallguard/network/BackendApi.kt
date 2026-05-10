package com.example.fallguard.network

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

// ---------------------------------------------------------
// Retrofit interface that maps mobile app actions to backend routes.
// ---------------------------------------------------------

interface BackendApi {

    @POST("/users/setup")
    suspend fun setupUser(@Body request: UserSetupRequest): Map<String, Any>

    @GET("/users/{userId}/home")
    suspend fun getHomeState(@Path("userId") userId: String): HomeStateResponse

    @POST("/devices/register")
    suspend fun registerDevice(@Body request: DeviceRegistration): Map<String, Any>

    @POST("/fall/start-countdown")
    suspend fun startCountdown(@Body request: StartCountdownRequest): IncidentResponse

    @POST("/fall/cancel")
    suspend fun cancelCountdown(@Body request: CancelCountdownRequest): IncidentResponse

    @POST("/incidents")
    suspend fun createIncident(@Body request: CreateIncidentRequest): IncidentResponse

    @POST("/incidents/{incidentId}/send-alerts")
    suspend fun sendAlerts(@Path("incidentId") incidentId: String): IncidentResponse

    @GET("/incidents/{incidentId}")
    suspend fun getIncident(@Path("incidentId") incidentId: String): IncidentResponse

    @POST("/incidents/{incidentId}/accept")
    suspend fun acceptIncident(
        @Path("incidentId") incidentId: String,
        @Body request: AcceptIncidentRequest
    ): IncidentResponse

    @POST("/incidents/{incidentId}/check-in")
    suspend fun submitCheckIn(
        @Path("incidentId") incidentId: String,
        @Body request: UserCheckInRequest
    ): IncidentResponse

    @POST("/incidents/{incidentId}/escalate-ems")
    suspend fun escalateToEms(@Path("incidentId") incidentId: String): IncidentResponse
}