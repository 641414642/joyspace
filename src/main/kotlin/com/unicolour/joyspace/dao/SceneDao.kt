package com.unicolour.joyspace.dao

import com.unicolour.joyspace.model.Scene
import org.springframework.data.repository.PagingAndSortingRepository

interface SceneDao : PagingAndSortingRepository<Scene, Int> {

    fun findByAlbumIdAndDeletedOrderByIndex(albumId: Int, deleted: Boolean): List<Scene>

}