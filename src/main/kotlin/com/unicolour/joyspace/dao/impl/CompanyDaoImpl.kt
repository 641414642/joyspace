package com.unicolour.joyspace.dao.impl

import com.unicolour.joyspace.dao.CompanyCustomQuery
import com.unicolour.joyspace.model.BusinessModel
import com.unicolour.joyspace.model.Company
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import java.util.*
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext
import javax.persistence.criteria.*

class CompanyDaoImpl : CompanyCustomQuery {
    @PersistenceContext
    lateinit var em: EntityManager

    override fun queryCompanies(name: String, businessModel: BusinessModel?): List<Company> {
        val cb = em.criteriaBuilder
        val cq = cb.createQuery(Company::class.java)

        val companyRoot = cq.from(Company::class.java)

        cq.select(companyRoot)

        where(name, businessModel, cb, cq, companyRoot)

        return em.createQuery(cq).resultList
    }

    override fun queryCompanies(pageable: Pageable, name: String, businessModel: BusinessModel?): Page<Company> {
        val cb = em.criteriaBuilder
        val cq = cb.createQuery(Company::class.java)
        val cqCount = cb.createQuery(Long::class.java)

        val companyRoot = cq.from(Company::class.java)
        val companyCountRoot = cqCount.from(Company::class.java)

        cq.select(companyRoot)
        cqCount.select(cb.count(companyCountRoot))

        where(name, businessModel, cb, cq, companyRoot)
        where(name, businessModel, cb, cqCount, companyRoot)

        orderBy(cb, pageable.sort, cq, companyRoot)

        val query = em.createQuery(cq).setFirstResult(pageable.offset).setMaxResults(pageable.pageSize)
        val countQuery = em.createQuery(cqCount)

        val count = countQuery.singleResult
        val companyList = query.resultList

        return PageImpl<Company>(companyList, pageable, count!!)
    }

    private fun orderBy(cb: CriteriaBuilder, sort: Sort, query: CriteriaQuery<Company>, root: Root<Company>) {
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

    private fun where(name: String, businessModel: BusinessModel?,
                      cb: CriteriaBuilder, cq: CriteriaQuery<*>, root: Root<Company>) {

        val conditions = ArrayList<Predicate>()

        if (name.isNotEmpty()) {
            conditions.add(cb.like(root.get("name"), "%$name%"))
        }

        if (businessModel != null) {
            conditions.add(cb.equal(root.get<Int>("businessModel"), businessModel.value))
        }

        cq.where(*conditions.toTypedArray())
    }
}