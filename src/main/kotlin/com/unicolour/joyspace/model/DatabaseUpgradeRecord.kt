package com.unicolour.joyspace.model

import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "database_upgrade_record")
class DatabaseUpgradeRecord {
    @Id
    var name: String = ""
}