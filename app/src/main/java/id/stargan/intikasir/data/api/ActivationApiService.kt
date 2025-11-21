package id.stargan.intikasir.data.api

import id.stargan.intikasir.data.model.ActivationRequest
import id.stargan.intikasir.data.model.ActivationResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface ActivationApiService {
    @POST("api/activate")
    suspend fun activate(@Body request: ActivationRequest): ActivationResponse
}

