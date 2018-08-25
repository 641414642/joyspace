package com.unicolour.joyspace.dao.impl

import com.unicolour.joyspace.dao.PrintStationCustomQuery
import com.unicolour.joyspace.model.PrintStation
import com.unicolour.joyspace.model.StationType
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import java.util.*
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext
import javax.persistence.criteria.*

class PrintStationDaoImpl : PrintStationCustomQuery {
    @PersistenceContext
    lateinit var em: EntityManager

    override fun queryPrintStations(companyId: Int, positionId: Int, printStationId: Int, name: String, stationType: StationType?, printerModel: String, onlineOnly: Boolean): List<PrintStation> {
        val cb = em.criteriaBuilder
        val cq = cb.createQuery(PrintStation::class.java)

        val printStationRoot = cq.from(PrintStation::class.java)

        cq.select(printStationRoot)

        where(companyId, positionId, printStationId, name, stationType, printerModel, onlineOnly, cb, cq, printStationRoot)

        return em.createQuery(cq).resultList
    }

    override fun queryPrintStations(pageable: Pageable, companyId: Int, positionId: Int, printStationId: Int, name: String, stationType: StationType?, printerModel: String, onlineOnly: Boolean): Page<PrintStation> {
        val cb = em.criteriaBuilder
        val cq = cb.createQuery(PrintStation::class.java)
        val cqCount = cb.createQuery(Long::class.java)

        val printStationRoot = cq.from(PrintStation::class.java)
        val printStationCountRoot = cqCount.from(PrintStation::class.java)

        cq.select(printStationRoot)
        cqCount.select(cb.count(printStationCountRoot))

        where(companyId, positionId, printStationId, name, stationType, printerModel, onlineOnly, cb, cq, printStationRoot)
        where(companyId, positionId, printStationId, name, stationType, printerModel, onlineOnly, cb, cqCount, printStationRoot)

        orderBy(cb, pageable.sort, cq, printStationRoot)

        val query = em.createQuery(cq).setFirstResult(pageable.offset).setMaxResults(pageable.pageSize)
        val countQuery = em.createQuery(cqCount)

        val count = countQuery.singleResult
        val printStationList = query.resultList

        return PageImpl<PrintStation>(printStationList, pageable, count!!)
    }

    private fun orderBy(cb: CriteriaBuilder, sort: Sort, query: CriteriaQuery<PrintStation>, root: Root<PrintStation>) {
        val orders = ArrayList<Order>()
        sort.forEach { order ->
            val direction = order.direction
            if (direction == Sort.Direction.ASC) {
                orders.add(cb.asc(root.get<Any>(order.property)))
            } else {
                orders.add(cb.desc(root.get<Any>(order.property)))
            }
        }

        query.orderBy(orders)
    }

    private fun where(companyId: Int, positionId: Int, printStationId: Int, name: String, stationType: StationType?,
                      printerModel: String, onlineOnly: Boolean,
                      cb: CriteriaBuilder, cq: CriteriaQuery<*>, root: Root<PrintStation>) {

        val conditions = ArrayList<Predicate>()

        if (name.isNotEmpty()) {
            conditions += cb.like(cb.lower(root.get("name")), "%${name.toLowerCase()}%")
        }

        if (companyId > 0) {
            conditions += cb.equal(root.get<Int>("companyId"), companyId)
        }

        if (positionId > 0) {
            conditions += cb.equal(root.get<Int>("positionId"), positionId)
        }

        if (printStationId > 0) {
            conditions += cb.equal(root.get<Int>("id"), printStationId)
        }

        if (printerModel.isNotEmpty()) {
            conditions += cb.like(cb.lower(root.get("printerModel")), "%${printerModel.toLowerCase()}%")
        }

        if (stationType != null) {
            conditions += cb.equal(root.get<Int>("stationType"), stationType.value)
        }

        if (onlineOnly) {
            val time = Calendar.getInstance()
            time.add(Calendar.SECOND, -30)

            conditions += cb.greaterThan(root.get("lastAccessTime"), time)
        }

        cq.where(*conditions.toTypedArray())
    }
}