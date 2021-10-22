package com.locart.profile

data class ProfileUser(
        val id: String,
        val name: String,
        val city: String,
        val url: String,

        val nexus: String,
        val job: String,
        val education: String,
        val website: String,
        val about: String,
        val company: String,
        val username: String,
        val lookingFor: String,
        val donate: String,
        val donateHow: String,

) {
    companion object {
        private var counter = 0L
    }
}
