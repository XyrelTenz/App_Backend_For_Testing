package com.xyrel.app.domain.entity

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "saved_places")
class SavedPlace(
    @Id @Column(name = "id", columnDefinition = "uuid") val id: UUID = UUID.randomUUID(),
    @Column(name = "user_id", nullable = false, columnDefinition = "uuid") val userId: UUID,
    @Column(name = "label", nullable = false) var label: String = "",
    @Column(name = "address", nullable = false) var address: String = "",
    @Column(name = "location", columnDefinition = "geography(Point,4326)", nullable = false)
    var location: String = "",
    @Column(name = "created_at", updatable = false) val createdAt: Instant = Instant.now(),
    @Column(name = "updated_at") var updatedAt: Instant = Instant.now(),
)
