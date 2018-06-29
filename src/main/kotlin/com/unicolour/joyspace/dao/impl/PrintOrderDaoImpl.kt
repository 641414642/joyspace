package com.unicolour.joyspace.dao.impl

import com.unicolour.joyspace.dao.PrintOrderCustomQuery
import com.unicolour.joyspace.dto.PrintOrderStatDTO
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

    override fun printOrderStat(companyId: Int, startTime: Calendar?, endTime: Calendar?,
                                payed: Boolean?, printed: Boolean?, printStationIds: List<Int>): PrintOrderStatDTO {
        val cb = em.criteriaBuilder
        val cq = cb.createTupleQuery()

        val orderRoot = cq.from(PrintOrder::class.java)

        cq.multiselect(
                cb.count(orderRoot),
                cb.sum(orderRoot.get("totalFee")),
                cb.sum(orderRoot.get("discount")),
                cb.sum(orderRoot.get("pageCount"))
        )

        where(companyId, printStationIds, startTime, endTime, payed, printed, cb, cq, orderRoot)

        val query = em.createQuery(cq)
        val result = query.singleResult

        val count = result[0] as Long
        val totalFee = result[1] as Long?
        val discount = result[2] as Long?
        val pageCount = result[3] as Long?

        return PrintOrderStatDTO(
                orderCount = count.toInt(),
                totalAmount = totalFee?.toInt() ?: 0,
                totalDiscount = discount?.toInt() ?: 0,
                printPageCount = pageCount?.toInt() ?: 0
        )
    }

    override fun queryPrintOrders(sort: Sort, companyId: Int, startTime: Calendar?, endTime: Calendar?, printStationIds: List<Int>): List<PrintOrder> {
        val cb = em.criteriaBuilder
        val cq = cb.createQuery(PrintOrder::class.java)

        val orderRoot = cq.from(PrintOrder::class.java)

        cq.select(orderRoot)

        where(companyId, printStationIds, startTime, endTime, null, null, cb, cq, orderRoot)

        orderBy(cb, sort, cq, orderRoot)

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

        where(companyId, printStationIds, startTime, endTime, null, null, cb, cq, orderRoot)
        where(companyId, printStationIds, startTime, endTime, null, null, cb, cqCount, orderRoot)

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
                      payed: Boolean?,
                      printed: Boolean?,
                      cb: CriteriaBuilder, cq: CriteriaQuery<*>, root: Root<PrintOrder>) {

        val conditions = ArrayList<Predicate>()

        if (startTime != null) {
            conditions.add(cb.greaterThanOrEqualTo(root.get("createTime"), startTime))
        }

        if (endTime != null) {
            conditions.add(cb.lessThan(root.get("createTime"), endTime))
        }

        if (payed != null) {
            conditions.add(cb.equal(root.get<Boolean>("payed"), payed))
        }

        if (printed != null) {
            conditions.add(cb.equal(root.get<Boolean>("printedOnPrintStation"), payed))
        }

        if (companyId > 0) {
            conditions.add(cb.equal(root.get<Int>("companyId"), companyId))
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

        cq.where(*conditions.toTypedArray())
    }
}