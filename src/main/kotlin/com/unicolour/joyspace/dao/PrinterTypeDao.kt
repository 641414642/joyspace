package com.unicolour.joyspace.dao

import com.unicolour.joyspace.model.PrinterType
import org.springframework.data.repository.CrudRepository

interface PrinterTypeDao : CrudRepository<PrinterType, String>
