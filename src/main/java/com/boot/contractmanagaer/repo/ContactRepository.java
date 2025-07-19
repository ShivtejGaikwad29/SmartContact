package com.boot.contractmanagaer.repo;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.boot.contractmanagaer.entity.Contacts;
import com.boot.contractmanagaer.entity.User;

public interface ContactRepository extends JpaRepository<Contacts,Integer>{
	@Query("from Contacts d where d.user.id = :userId")
//	contact per page & current page is in pageable.
	public Page<Contacts> findContactsByUser(@Param("userId") int userId,Pageable pageable);
	public Contacts findByPhoneAndUser(String phone, User user);
//	public Page<Contacts> findContactsByUser(int id);
	
//	search bar
	public List<Contacts> findByNameContainingAndUser(String name , User user);
}
