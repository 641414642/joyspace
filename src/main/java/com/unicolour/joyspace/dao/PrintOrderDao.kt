package com.unicolour.joyspace.dao

import com.unicolour.joyspace.model.PrintOrder
import org.springframework.data.repository.CrudRepository

interface PrintOrderDao : CrudRepository<PrintOrder, Int>