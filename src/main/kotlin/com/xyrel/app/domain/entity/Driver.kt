package com.xyrel.app.domain.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "drivers")
class Driver(
    @Id @Column(name = "user_id", columnDefinition = "uuid") val userId: UUID,
    @OneToOne(fetch = FetchType.LAZY) @MapsId @JoinColumn(name = "user_id") var user: User? = null,
    @Column(name = "plate_number", nullable = false, unique = true) var plateNumber: String = "",
    @Column(name = "vehicle_type", nullable = false) var vehicleType: String = "baobao_etrike",
    @Column(name = "vehicle_color") var vehicleColor: String? = null,
    @Column(name = "license_number", nullable = false) var licenseNumber: String = "",
    @Column(name = "is_verified") var isVerified: Boolean = false,
    @Column(name = "is_online") var isOnline: Boolean = false,
    @Column(name = "total_cash_collected", precision = 15, scale = 2)
    var totalCashCollected: BigDecimal = BigDecimal.ZERO,
    @Column(name = "commission_owed_to_platform", precision = 15, scale = 2)
    var commissionOwedToPlatform: BigDecimal = BigDecimal.ZERO,

    // Stored as PostGIS GEOGRAPHY(Point, 4326) — WKT "POINT(lng lat)". Managed via native queries
    // for spatial ops.
    @Column(name = "last_known_location", columnDefinition = "geography(Point,4326)")
    // WKT: "POINT(longitude latitude)"
    var lastKnownLocation: String? = null,
    @Column(name = "updated_at") var updatedAt: Instant = Instant.now(),
)
