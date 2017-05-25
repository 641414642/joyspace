package com.unicolour.joyspace.dao;

import com.unicolour.joyspace.model.Manager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

public interface ManagerDao extends PagingAndSortingRepository<Manager, Integer> {
	Manager findById(int id);

	Page<Manager> findAll(Pageable pageRequest);

	@Query("SELECT u FROM Manager u WHERE LOWER(u.userName) LIKE LOWER(CONCAT('%', :name, '%')) OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :name, '%'))")
	Page<Manager> findByUserNameOrFullName(@Param("name") String name, Pageable pageRequest);

	Manager findByUserNameOrFullName(String userName, String fullName);

	Manager findByUserName(String userName);
}
