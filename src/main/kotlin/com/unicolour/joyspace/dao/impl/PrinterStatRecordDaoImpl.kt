package com.unicolour.joyspace.dao.impl

import com.unicolour.joyspace.dao.PrinterStatRecordCustomQuery
import com.unicolour.joyspace.model.PrinterStatRecord
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext

class PrinterStatRecordDaoImpl : PrinterStatRecordCustomQuery  {
    @PersistenceContext
    lateinit var em: EntityManager

    override fun getLastMessageRecord(printStationId: Int): PrinterStatRecord? {
        val cb = em.criteriaBuilder
        val cq = cb.createQuery(PrinterStatRecord::class.java)
        val root = cq.from(PrinterStatRecord::class.java)

        cq.select(root)
        cq.where(
                cb.equal(root.get<Int>("printStationId"), printStationId),
                cb.isNotNull(root.get<Any>("sendToPhoneNumber"))
        )

        cq.orderBy(cb.desc(root.get<Any>("id")))

        val query = em.createQuery(cq)
        query.firstResult = 0
        query.maxResults = 1

        return query.resultList?.firstOrNull()
    }
}