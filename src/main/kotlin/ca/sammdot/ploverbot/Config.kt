package ca.sammdot.ploverbot

import com.google.gson.annotations.SerializedName

data class Config(
  @SerializedName("token")
  val token: String,

  @SerializedName("server")
  val server: Long,

  @SerializedName("theories")
  val theories: Map<String, String>,

  @SerializedName("channels")
  val channelMap: Map<Long, String>,
)
