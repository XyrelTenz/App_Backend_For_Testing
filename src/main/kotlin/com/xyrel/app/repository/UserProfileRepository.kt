package com.xyrel.app.repository

import com.xyrel.app.domain.entity.UserProfile
import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository interface UserProfileRepository : JpaRepository<UserProfile, UUID>
