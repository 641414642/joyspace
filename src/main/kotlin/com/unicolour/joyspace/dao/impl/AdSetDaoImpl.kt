package com.unicolour.joyspace.dao.impl

import com.unicolour.joyspace.dao.AdSetCustomQuery
import com.unicolour.joyspace.model.AdSet
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import java.util.*
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext
import javax.persistence.criteria.*

class AdSetDaoImpl : AdSetCustomQuery {
    @PersistenceContext
    lateinit var em: EntityManager

    override fun queryAdSets(companyId: Int?, name: String, includePublicResource: Boolean): List<AdSet> {
        val cb = em.criteriaBuilder
        val cq = cb.createQuery(AdSet::class.java)

        val adSetRoot = cq.from(AdSet::class.java)

        cq.select(adSetRoot)

        where(companyId, name, includePublicResource, cb, cq, adSetRoot)

        return em.createQuery(cq).resultList
    }

    override fun queryAdSets(pageable: Pageable, companyId: Int?, name: String, includePublicResource: Boolean): Page<AdSet> {
        val cb = em.criteriaBuilder
        val cq = cb.createQuery(AdSet::class.java)
        val cqCount = cb.createQuery(Long::class.java)

        val adSetRoot = cq.from(AdSet::class.java)
        val adSetCountRoot = cqCount.from(AdSet::class.java)

        cq.select(adSetRoot)
        cqCount.select(cb.count(adSetCountRoot))

        where(companyId, name, includePublicResource, cb, cq, adSetRoot)
        where(companyId, name, includePublicResource, cb, cqCount, adSetRoot)

        orderBy(cb, pageable.sort, cq, adSetRoot)

        val query = em.createQuery(cq).setFirstResult(pageable.offset).setMaxResults(pageable.pageSize)
        val countQuery = em.createQuery(cqCount)

        val count = countQuery.singleResult
        val adSetList = query.resultList

        return PageImpl<AdSet>(adSetList, pageable, count!!)
    }

    private fun orderBy(cb: CriteriaBuilder, sort: Sort, query: CriteriaQuery<AdSet>, root: Root<AdSet>) {
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

    private fun where(companyId: Int?, name: String, includePublicResource: Boolean,
                      cb: CriteriaBuilder, cq: CriteriaQuery<*>, root: Root<AdSet>) {

        val conditions = ArrayList<Predicate>()

        if (name.isNotEmpty()) {
            conditions.add(cb.like(root.get("name"), "%$name%"))
        }

        if (companyId != null) {
            val companyIdField = root.get<Int>("companyId")
            if (includePublicResource) {
                conditions.add(cb.or(
                        cb.equal(companyIdField, companyId),
                        cb.equal(companyIdField, 0))
                )
            } else {
                conditions.add(cb.equal(companyIdField, companyId))
            }
        }

        cq.where(*conditions.toTypedArray())
    }
}