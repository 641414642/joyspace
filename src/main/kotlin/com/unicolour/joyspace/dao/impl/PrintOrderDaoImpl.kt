package com.unicolour.joyspace.dao.impl

import com.unicolour.joyspace.dao.PrintOrderCustomQuery
import com.unicolour.joyspace.model.PrintOrder
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import java.util.*
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext
import javax.persistence.criteria.*

class PrintOrderDaoImpl : PrintOrderCustomQuery  {
    @PersistenceContext
    lateinit var em: EntityManager

    override fun queryPrintOrders(companyId: Int, startTime: Calendar?, endTime: Calendar?, printStationIds: List<Int>): List<PrintOrder> {
        val cb = em.criteriaBuilder
        val cq = cb.createQuery(PrintOrder::class.java)

        val orderRoot = cq.from(PrintOrder::class.java)

        cq.select(orderRoot)

        where(companyId, printStationIds, startTime, endTime, cb, cq, orderRoot)

        val query = em.createQuery(cq)

        return query.resultList
    }

    override fun queryPrintOrders(pageable: Pageable, companyId: Int, startTime: Calendar?, endTime: Calendar?, printStationIds: List<Int>): Page<PrintOrder> {
        val cb = em.criteriaBuilder
        val cq = cb.createQuery(PrintOrder::class.java)
        val cqCount = cb.createQuery(Long::class.java)

        val orderRoot = cq.from(PrintOrder::class.java)
        val orderCountRoot = cqCount.from(PrintOrder::class.java)

        cq.select(orderRoot)
        cqCount.select(cb.count(orderCountRoot))

        where(companyId, printStationIds, startTime, endTime, cb, cq, orderRoot)
        where(companyId, printStationIds, startTime, endTime, cb, cqCount, orderRoot)

        orderBy(cb, pageable.sort, cq, orderRoot)

        val query = em.createQuery(cq).setFirstResult(pageable.offset).setMaxResults(pageable.pageSize)
        val countQuery = em.createQuery(cqCount)

        val count = countQuery.singleResult
        val orderList = query.resultList

        return PageImpl<PrintOrder>(orderList, pageable, count!!)
    }

    private fun orderBy(cb: CriteriaBuilder, sort: Sort, query: CriteriaQuery<PrintOrder>, root: Root<PrintOrder>) {
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

    private fun where(companyId: Int, printStationIds: List<Int>,
                      startTime: Calendar?, endTime: Calendar?,
                      cb: CriteriaBuilder, cq: CriteriaQuery<*>, root: Root<PrintOrder>) {

        val conditions = ArrayList<Predicate>()

        if (startTime != null) {
            conditions.add(cb.greaterThanOrEqualTo(root.get("createTime"), startTime))
        }

        if (endTime != null) {
            val end = endTime.clone() as Calendar
            end.add(Calendar.DAY_OF_MONTH, 1)
            conditions.add(cb.lessThan(root.get("createTime"), end))
        }

        if (printStationIds.isNotEmpty()) {
            if (printStationIds.size == 1) {
                conditions.add(cb.equal(root.get<Int>("printStationId"), printStationIds.first()))
            }
            else {
                val path = root.get<Int>("printStationId")
                val inElement = cb.`in`(path)
                printStationIds.forEach { inElement.value(it) }
                conditions.add(inElement)
            }
        }
        else {
            conditions.add(cb.equal(root.get<Int>("companyId"), companyId))
        }

        cq.where(*conditions.toTypedArray())
    }
}