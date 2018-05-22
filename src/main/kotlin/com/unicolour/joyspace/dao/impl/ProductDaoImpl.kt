package com.unicolour.joyspace.dao.impl

import com.unicolour.joyspace.dao.ProductCustomQuery
import com.unicolour.joyspace.model.Product
import org.springframework.data.domain.*
import java.util.*
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext
import javax.persistence.criteria.*

class ProductDaoImpl : ProductCustomQuery  {
    @PersistenceContext
    lateinit var em: EntityManager

    override fun queryProducts(companyId: Int, name: String, excludeDeleted: Boolean, sort: Sort): List<Product> {
        val cb = em.criteriaBuilder
        val cq = cb.createQuery(Product::class.java)

        val productRoot = cq.from(Product::class.java)

        cq.select(productRoot)

        where(companyId, name, excludeDeleted, cb, cq, productRoot)
        orderBy(cb, sort, cq, productRoot)

        return em.createQuery(cq).resultList
    }

    override fun queryProducts(pageable: Pageable, companyId: Int, name: String, excludeDeleted: Boolean): Page<Product> {
        val cb = em.criteriaBuilder
        val cq = cb.createQuery(Product::class.java)
        val cqCount = cb.createQuery(Long::class.java)

        val productRoot = cq.from(Product::class.java)
        val productCountRoot = cqCount.from(Product::class.java)

        cq.select(productRoot)
        cqCount.select(cb.count(productCountRoot))

        where(companyId, name, excludeDeleted, cb, cq, productRoot)
        where(companyId, name, excludeDeleted, cb, cqCount, productRoot)

        orderBy(cb, pageable.sort, cq, productRoot)

        val query = em.createQuery(cq).setFirstResult(pageable.offset).setMaxResults(pageable.pageSize)
        val countQuery = em.createQuery(cqCount)

        val count = countQuery.singleResult
        val productList = query.resultList

        return PageImpl<Product>(productList, pageable, count!!)
    }

    private fun orderBy(cb: CriteriaBuilder, sort: Sort, query: CriteriaQuery<Product>, root: Root<Product>) {
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

    private fun where(companyId: Int, name: String, excludeDeleted: Boolean,
                      cb: CriteriaBuilder, cq: CriteriaQuery<*>, root: Root<Product>) {

        val conditions = ArrayList<Predicate>()

        if (name.isNotEmpty()) {
            conditions.add(cb.like(root.get("name"), "%$name%"))
        }

        if (excludeDeleted) {
            conditions.add(cb.equal(root.get<Boolean>("deleted"), false))
        }

        if (companyId > 0) {
            val companyIdField = root.get<Int>("companyId")
            conditions.add(cb.or(
                    cb.equal(companyIdField, companyId),
                    cb.equal(companyIdField, 0))
            )
        }

        cq.where(*conditions.toTypedArray())
    }
}