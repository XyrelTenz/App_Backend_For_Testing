package com.xyrel.app.domain.entity

import com.xyrel.app.domain.enums.UserRole
import jakarta.persistence.*
import java.time.Instant
import java.util.UUID
import org.hibernate.annotations.SQLRestriction

@Entity
@Table(name = "users")
@SQLRestriction("deleted_at IS NULL")
class User(
    @Id @Column(name = "id", columnDefinition = "uuid") val id: UUID = UUID.randomUUID(),
    @Column(name = "firebase_uid", unique = true) var firebaseUid: String? = null,
    @Column(name = "password_hash") var passwordHash: String? = null,
    @Column(name = "email", nullable = false, unique = true, columnDefinition = "citext")
    var email: String = "",
    @Column(name = "phone", unique = true, columnDefinition = "citext") var phone: String? = null,
    @Column(name = "full_name", nullable = false) var fullName: String = "",
    @Enumerated(EnumType.STRING)
    @Column(name = "role", columnDefinition = "user_role")
    var role: UserRole = UserRole.NONE,
    @Column(name = "is_active") var isActive: Boolean = true,
    @Column(name = "deleted_at") var deletedAt: Instant? = null,
    @Column(name = "last_seen_at") var lastSeenAt: Instant? = null,
    @Column(name = "created_at", updatable = false) val createdAt: Instant = Instant.now(),
    @Column(name = "updated_at") var updatedAt: Instant = Instant.now(),
    @OneToOne(mappedBy = "user", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var profile: UserProfile? = null,
    @OneToOne(mappedBy = "user", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var driver: Driver? = null,
)
