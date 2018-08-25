package com.unicolour.joyspace.dao.impl

import com.unicolour.joyspace.dao.TemplateCustomQuery
import com.unicolour.joyspace.model.ProductType
import com.unicolour.joyspace.model.Template
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import java.util.*
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext
import javax.persistence.criteria.*

class TemplateDaoImpl : TemplateCustomQuery {
    @PersistenceContext
    lateinit var em: EntityManager


    override fun queryTemplates(type: ProductType?, name: String, excludeDeleted: Boolean, sort: Sort): List<Template> {
        val cb = em.criteriaBuilder
        val cq = cb.createQuery(Template::class.java)

        val templateRoot = cq.from(Template::class.java)

        cq.select(templateRoot)

        where(type, name, excludeDeleted, cb, cq, templateRoot)
        orderBy(cb, sort, cq, templateRoot)

        return em.createQuery(cq).resultList
    }

    override fun queryTemplates(pageable: Pageable, type: ProductType?, name: String, excludeDeleted: Boolean): Page<Template> {
        val cb = em.criteriaBuilder
        val cq = cb.createQuery(Template::class.java)
        val cqCount = cb.createQuery(Long::class.java)

        val templateRoot = cq.from(Template::class.java)
        val templateCountRoot = cqCount.from(Template::class.java)

        cq.select(templateRoot)
        cqCount.select(cb.count(templateCountRoot))

        where(type, name, excludeDeleted, cb, cq, templateRoot)
        where(type, name, excludeDeleted, cb, cqCount, templateRoot)

        orderBy(cb, pageable.sort, cq, templateRoot)

        val query = em.createQuery(cq).setFirstResult(pageable.offset).setMaxResults(pageable.pageSize)
        val countQuery = em.createQuery(cqCount)

        val count = countQuery.singleResult
        val templateList = query.resultList

        return PageImpl<Template>(templateList, pageable, count!!)
    }

    private fun orderBy(cb: CriteriaBuilder, sort: Sort, query: CriteriaQuery<Template>, root: Root<Template>) {
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

    private fun where(type: ProductType?, name: String, excludeDeleted: Boolean,
                      cb: CriteriaBuilder, cq: CriteriaQuery<*>, root: Root<Template>) {

        val conditions = ArrayList<Predicate>()

        if (name.isNotEmpty()) {
            conditions.add(cb.like(root.get("name"), "%$name%"))
        }

        if (excludeDeleted) {
            conditions.add(cb.equal(root.get<Boolean>("deleted"), false))
        }

        if (type != null) {
            conditions.add(cb.equal(root.get<Int>("type"), type.value))
        }else{
            conditions.add(cb.notEqual(root.get<Int>("type"), ProductType.SCENE.value))
        }

        cq.where(*conditions.toTypedArray())
    }
}