package com.xyrel.app.domain.entity

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "ratings")
class Rating(
    @Id @Column(name = "id", columnDefinition = "uuid") val id: UUID = UUID.randomUUID(),
    @Column(name = "ride_id", nullable = false, columnDefinition = "uuid") val rideId: UUID,
    @Column(name = "rater_id", nullable = false, columnDefinition = "uuid") val raterId: UUID,
    @Column(name = "rated_id", nullable = false, columnDefinition = "uuid") val ratedId: UUID,
    @Column(name = "score", nullable = false) val score: Int,
    @Column(name = "comment") var comment: String? = null,
    @Column(name = "created_at", updatable = false) val createdAt: Instant = Instant.now(),
    @Column(name = "updated_at") var updatedAt: Instant = Instant.now(),
)
