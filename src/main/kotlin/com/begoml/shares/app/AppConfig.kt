package com.begoml.shares.app

/**
 * Runtime configuration read from environment variables.
 * Secrets have no defaults: the server refuses to start without them.
 */
class AppConfig(
    val databaseShardUrls: List<String>,
    val databaseSharesUrl: String,
    val databaseUser: String,
    val databasePassword: String,
    val jwtSecret: String,
    val legacyPasswordSecret: String,
    val redisHost: String,
    val redisPort: Int,
    val rabbitHost: String,
    val rabbitPort: Int,
) {
    companion object {
        // Key of the old HMAC "password hash". Only used to verify (and then re-hash with bcrypt)
        // passwords stored before the bcrypt migration; it is already public in git history.
        private const val LEGACY_PASSWORD_SECRET_DEFAULT = "begoml-shares-secret-key"

        fun fromEnv(env: Map<String, String> = System.getenv()): AppConfig {
            fun optional(name: String, default: String) = env[name]?.takeIf { it.isNotBlank() } ?: default
            fun required(name: String) = env[name]?.takeIf { it.isNotBlank() }
                ?: error("Environment variable $name is not set")

            return AppConfig(
                databaseShardUrls = listOf(
                    optional("DATABASE_URL_SHARD_1", "jdbc:postgresql://localhost:5432/begoml_share"),
                    optional("DATABASE_URL_SHARD_2", "jdbc:postgresql://localhost:5432/begoml_shares_second"),
                ),
                databaseSharesUrl = optional("DATABASE_URL_SHARES", "jdbc:postgresql://localhost:5432/begoml_shares_shares"),
                databaseUser = optional("DATABASE_USER", "postgres"),
                databasePassword = required("DATABASE_PASSWORD"),
                jwtSecret = required("JWT_SECRET"),
                legacyPasswordSecret = optional("LEGACY_PASSWORD_SECRET", LEGACY_PASSWORD_SECRET_DEFAULT),
                redisHost = optional("REDIS_HOST", "localhost"),
                redisPort = optional("REDIS_PORT", "6379").toInt(),
                rabbitHost = optional("RABBITMQ_HOST", "localhost"),
                rabbitPort = optional("RABBITMQ_PORT", "5672").toInt(),
            )
        }
    }
}
