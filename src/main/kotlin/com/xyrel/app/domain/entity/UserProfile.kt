package com.xyrel.app.domain.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

@Entity
@Table(name = "user_profiles")
class UserProfile(
    @Id @Column(name = "user_id", columnDefinition = "uuid") val userId: UUID,
    @OneToOne(fetch = FetchType.LAZY) @MapsId @JoinColumn(name = "user_id") var user: User? = null,
    @Column(name = "profile_image_url") var profileImageUrl: String? = null,
    @Column(name = "country") var country: String = "Philippines",
    @Column(name = "address") var address: String? = null,
    @Column(name = "dob") var dob: LocalDate? = null,
    @Column(name = "average_rating", precision = 3, scale = 2)
    var averageRating: BigDecimal = BigDecimal("5.0"),
    @Column(name = "total_trips_completed") var totalTripsCompleted: Int = 0,
    @Column(name = "updated_at") var updatedAt: Instant = Instant.now(),
)
