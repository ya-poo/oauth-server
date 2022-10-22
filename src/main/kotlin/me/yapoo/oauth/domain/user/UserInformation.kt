package me.yapoo.oauth.domain.user

import java.time.LocalDate

// OpenID Connect Core 1.0 - 5.1 の標準クレームをドメインモデルとして持っておく
data class UserInformation(
    val id: UserSubject,
    // given, family. middle はサポートしない
    val name: String?,
    val nickname: String?,
    val preferredUsername: String?,
    val email: String?,
    val emailVerified: Boolean?,
    val gender: Gender?,
    val birthdate: LocalDate?,
)

enum class Gender {
    Male,
    Female,
    ;
}
