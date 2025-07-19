package com.boot.contractmanagaer.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.boot.contractmanagaer.entity.Contacts;
import com.boot.contractmanagaer.entity.User;
import com.boot.contractmanagaer.repo.ContactRepository;
import com.boot.contractmanagaer.repo.UserRepository;

@RestController
public class SearchController {
	
	@Autowired
	private UserRepository userrepo;
	
	@Autowired
	private ContactRepository contactrepo;
	
//	search handler
	@GetMapping("/search/{query}")
	public ResponseEntity<?> search(@PathVariable("query") String query ,Principal prin){
		System.out.print(query);
		User user = this.userrepo.getByUserName(prin.getName());
		List<Contacts> contact = this.contactrepo.findByNameContainingAndUser(query, user);
		return ResponseEntity.ok(contact);
	}
}
