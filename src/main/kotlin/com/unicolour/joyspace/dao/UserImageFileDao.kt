package com.unicolour.joyspace.dao

import com.unicolour.joyspace.model.UserImageFile
import org.springframework.data.repository.CrudRepository

interface UserImageFileDao : CrudRepository<UserImageFile, Int>
