package me.yapoo.oauth.router.error

/**
 * RFC でエラーの型が定義されていない場合に用いる共通のエラーレスポンス
 */
data class ErrorResponse(
    val error: String,
    val errorDescription: String,
)
