package com.boot.contractmanagaer.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.boot.contractmanagaer.ContractManagerApplication;
import com.boot.contractmanagaer.entity.Contacts;
import com.boot.contractmanagaer.entity.User;
import com.boot.contractmanagaer.helper.Message;
import com.boot.contractmanagaer.repo.ContactRepository;
import com.boot.contractmanagaer.repo.UserRepository;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/user")
public class UserController {

	private final ContractManagerApplication contractManagerApplication;
	
	@Autowired
	private BCryptPasswordEncoder bcryptpasswordencoder;
	
	@Autowired
	private UserRepository userrepo;

	@Autowired
	private ContactRepository contactrepo;

	UserController(ContractManagerApplication contractManagerApplication) {
		this.contractManagerApplication = contractManagerApplication;
	}

	@ModelAttribute
	public void addCommanAttribute(Model model, Principal prin) {
		String username = prin.getName();
		System.out.println("username " + username);
		User user = userrepo.getByUserName(username);
		System.out.println("user " + user);
		model.addAttribute("user", user);
	}

//	dashboard
	@GetMapping("/index")
	public String dashboard(Model model, Principal prin) {
		model.addAttribute("title", "User dashboard");
		return "normal/dashboard";
	}

//	profile
	@GetMapping("/profile")
	public String profile(Model model, Principal principal) {
		String username = principal.getName(); // assuming username = email
		User user = this.userrepo.getByUserName(username); // or findByUsername
		model.addAttribute("user", user);
		model.addAttribute("title", "User Profile");
		return "normal/profile";
	}

	@GetMapping("/add-contact")	
	public String openAddContactForm(Model model) {
		model.addAttribute("contacts", new Contacts()); // Ensure this line exists
		return "normal/add_contact_form";
	}

	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute Contacts contact, Principal principal,
			@RequestParam("Profileimage") MultipartFile file, RedirectAttributes redirectAttributes) {
		try {
			String name = principal.getName();
			User user = this.userrepo.getByUserName(name);

			if (file.isEmpty()) {
				System.out.println("File is empty..!");
				contact.setImage("contact.jpg");
			} else {
				contact.setImage(file.getOriginalFilename());
				File saveFile = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				System.out.println("File is uploaded...");
			}
			Contacts existingPhone = contactrepo.findByPhoneAndUser(contact.getPhone(), user);
			if (existingPhone != null) {
				redirectAttributes.addFlashAttribute("error", "Phone number already exists. Please try another one.");
				return "redirect:/user/add-contact";
			}

			contact.setUser(user);
			user.getCont().add(contact);
			this.userrepo.save(user);

			// ✅ Flash message on success
			redirectAttributes.addFlashAttribute("message",
					new Message("Contact saved successfully!", "alert-success"));

		} catch (DataIntegrityViolationException e) {
			redirectAttributes.addFlashAttribute("error", "Email already exists. Please try another one.");
			return "redirect:/user/add-contact";
		} catch (Exception e) {
			e.printStackTrace();
			redirectAttributes.addFlashAttribute("message",
					new Message("Something went wrong. Please try again.", "alert-danger"));
			return "redirect:/user/add-contact";
		}

		// ✅ Always redirect after POST
		return "redirect:/user/show_contacts/0";
	}

//	controller for showing the users

	@GetMapping("/show_contacts/{page}")
	public String showContacts(@PathVariable("page") Integer page, Model m, Principal pricipal) {
		m.addAttribute("title", "Show Contact Page");
		String username = pricipal.getName();
		User user = this.userrepo.getByUserName(username);
		PageRequest pageable = PageRequest.of(page, 5);
		Page<Contacts> contacts = this.contactrepo.findContactsByUser(user.getId(), pageable);
		m.addAttribute("contacts", contacts);
		m.addAttribute("user", user);
		m.addAttribute("currentpage", page);
		m.addAttribute("totalpages", contacts.getTotalPages());

		return "normal/show_contacts";
	}

//	mapping for showing the contact details after clicking the specific contact
	@RequestMapping("/{cid}/contact")
	public String ShowContactDetail(@PathVariable("cid") Integer cid, Model m, Principal prin) {
		Optional<Contacts> contactOptional = this.contactrepo.findById(cid);
		String name = prin.getName();
		User user = this.userrepo.getByUserName(name);

		if (contactOptional.isPresent()) {
			Contacts contact = contactOptional.get();
			if (user.getId() == contact.getUser().getId()) {
				m.addAttribute("contact", contact); // user is allowed
				m.addAttribute("title", user.getName());
			} else {
				m.addAttribute("contact", null); // user is not allowed
			}
		} else {
			m.addAttribute("contact", null); // contact doesn't exist
		}

		m.addAttribute("user", user);
		return "normal/contact_details";
	}

	@GetMapping("/delete/{cid}")
	public String DeleteContact(@PathVariable("cid") Integer cid, HttpSession session, Principal principal) {

		String username = principal.getName();
		User user = this.userrepo.getByUserName(username); // make sure this method exists

		Contacts contact = this.contactrepo.findById(cid)
				.orElseThrow(() -> new IllegalArgumentException("Invalid contact ID: " + cid));

		// Check ownership before deletion
		if (contact.getUser().getId() == user.getId()) {
			user.getCont().remove(contact); // Remove from list
			this.userrepo.save(user); // Triggers orphan removal

			session.setAttribute("message", new Message("Contact deleted successfully!", "success"));
		} else {
			session.setAttribute("message", new Message("You are not authorized to delete this contact", "danger"));
		}

		return "redirect:/user/show_contacts/0";
	}

//	to open the update form
	@GetMapping("/update/{cid}")
	public String showUpdateForm(@PathVariable("cid") Integer cid, Model m) {
		Contacts contact = this.contactrepo.findById(cid).orElse(null);
		if (contact != null) {
			m.addAttribute("contacts", contact);
		}
		return "normal/update_contact"; // This is the form to update the contact
	}

//	update contact handler

	@PostMapping("/processupdate")
	public String updateHandler(@ModelAttribute Contacts contact, @RequestParam("Profileimage") MultipartFile file,
			HttpSession session, Principal principal) {
		try {
			User user = this.userrepo.getByUserName(principal.getName());

			// Fetch old contact from DB
			Contacts oldContact = contactrepo.findById(contact.getCid())
					.orElseThrow(() -> new IllegalArgumentException("Invalid contact ID: " + contact.getCid()));

			// Image update
			if (!file.isEmpty()) {
				// Delete old image if not default
				if (!oldContact.getImage().equals("contact.jpg")) {
					File deleteFile = new ClassPathResource("static/img").getFile();
					File oldFile = new File(deleteFile, oldContact.getImage());
					oldFile.delete();
				}

				// Upload new image
				contact.setImage(file.getOriginalFilename());
				File saveFile = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
			} else {
				// Keep existing image
				contact.setImage(oldContact.getImage());
			}

			contact.setUser(user);
			contactrepo.save(contact);
			session.setAttribute("message", new Message("Contact updated successfully!", "success"));

		} catch (Exception e) {
			e.printStackTrace();
			session.setAttribute("message", new Message("Update failed!", "danger"));
		}

		return "redirect:/user/show_contacts/0";
	}
	
	@GetMapping("/settings")
	public String openSettings() {
		
		return "normal/settings";
	}
	
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("oldpassword") String oldpassword , @RequestParam("newpassword") String newpassword , Principal prin , HttpSession session) {
//		System.out.println("oldpass"+oldpassword);
//		System.out.println("newpass"+newpassword);
		String username = prin.getName();
		User currentuser = this.userrepo.getByUserName(username);
//		System.out.println(currentuser.getPassword());
		
		if(this.bcryptpasswordencoder.matches(oldpassword,currentuser.getPassword())) {
			currentuser.setPassword(this.bcryptpasswordencoder.encode(newpassword));
			this.userrepo.save(currentuser);
			session.setAttribute("message", new Message("Password Is Successfully Changed", "success"));
		}else {
			session.setAttribute("message", new Message("Update failed! Please Enter Correct Old Password", "danger"));
			return "redirect:/user/settings";
		}
		return "redirect:/user/index";
	}
	
//	@GetMapping("/dashboard")
//	public String dashboardprofile(Model model, Principal principal) {
//	    User user = userrepo.getByUserName(principal.getName());
//
//	    model.addAttribute("user", user);
//
//	    Page<Contacts> contacts = this.contactrepo.findContactsByUser1(user.getId());
//
//	    model.addAttribute("contacts", contacts);
//	    model.addAttribute("totalPages", contacts.getTotalPages());
//
//	    // ✅ Use contacts.getTotalElements() instead of countByUser
//	    model.addAttribute("totalContacts", contacts.getTotalElements());

//	    model.addAttribute("newThisWeek", contactrepo.countNewThisWeek(user));
//	    model.addAttribute("favoriteCount", contactrepo.countFavorites(user));
//	    model.addAttribute("recentActivities", activityService.getRecentActivities(user));

//	    return "normal/dashboard";
//	}

}
