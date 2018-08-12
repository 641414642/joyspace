package com.unicolour.joyspace.dao

import com.unicolour.joyspace.model.DatabaseUpgradeRecord
import org.springframework.data.repository.CrudRepository

interface DatabaseUpgradeRecordDao : CrudRepository<DatabaseUpgradeRecord, String>
