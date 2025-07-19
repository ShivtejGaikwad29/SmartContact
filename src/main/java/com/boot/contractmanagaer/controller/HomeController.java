package com.boot.contractmanagaer.controller;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.boot.contractmanagaer.entity.User;
import com.boot.contractmanagaer.helper.Message;
import com.boot.contractmanagaer.repo.UserRepository;
//import com.sun.org.slf4j.internal.LoggerFactory;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
public class HomeController {

	@Autowired
	private BCryptPasswordEncoder passwordEncoder;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private DataSource dataSource;

	@GetMapping("/")
	public String home() {
		return "home";
	}

	@GetMapping("/about")
	public String about() {
		return "about";
	}

	@GetMapping("/signup")
	public String signup(Model model, HttpSession session) {
	    model.addAttribute("user", new User());

	    Object messageObj = session.getAttribute("message");
	    if (messageObj != null && messageObj instanceof Message) {
	        model.addAttribute("message", (Message) messageObj);
	        session.removeAttribute("message");
	    }

	    return "signup";
	}

	@GetMapping("faillogin")
	public String failLogin() {
		return "faillogin"; // This should match your filename: faillogin.html
	}

	@GetMapping("/login")
	public String login() {
		return "login";
	}

	@PostMapping("/do_register")
	public String registerUser(@Valid @ModelAttribute("user") User user, BindingResult result,
			@RequestParam(value = "agreement", defaultValue = "false") boolean agreement, Model model,
			HttpSession session, RedirectAttributes redirectAttributes) {

		System.out.println("Inside registerUser method");

		try {
			// Check if the user agrees to the terms and conditions
			if (!agreement) {
				throw new Exception("You must agree to the terms and conditions");
			}

			// Check if there are validation errors in the form submission
			if (result.hasErrors()) {
				model.addAttribute("user", user);
				return "signup";
			}

			// Set user properties
			user.setRole("ROLE_USER");
			user.setEnabled(true);
			user.setImage("default.png");
			user.setPassword(passwordEncoder.encode(user.getPassword()));

			// Save the user to the database
			userRepository.save(user);

			// Add success message and redirect to login page
			redirectAttributes.addFlashAttribute("message", new Message("Successfully registered!", "alert-success"));
			return "redirect:/login";

		} catch (DataIntegrityViolationException e) {
			// Handle case where email already exists
			redirectAttributes.addFlashAttribute("message",
					new Message("Email already exists. Please try another one.", "alert-danger"));
			return "redirect:/signup";
		} catch (Exception e) {
			// Handle general errors
			redirectAttributes.addFlashAttribute("message",
					new Message("Something went wrong! " + e.getMessage(), "alert-danger"));
			return "redirect:/signup";
		}
	}

	@GetMapping("/test-db-connection")
	public String testDbConnection() {
		try (Connection connection = dataSource.getConnection()) {
			if (connection != null) {
				return "Database connected successfully!";
			} else {
				return "Failed to connect to database!";
			}
		} catch (SQLException e) {
			return "Error: " + e.getMessage();
		}
	}
}
