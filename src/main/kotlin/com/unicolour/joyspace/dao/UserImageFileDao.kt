package com.unicolour.joyspace.dao

import com.unicolour.joyspace.model.Manager
import com.unicolour.joyspace.model.UserImageFile
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.data.repository.query.Param

interface UserImageFileDao : CrudRepository<UserImageFile, Int>
