package com.boot.contractmanagaer.controller;

import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.boot.contractmanagaer.email.EmailService;
import com.boot.contractmanagaer.entity.User;
import com.boot.contractmanagaer.helper.Message;
import com.boot.contractmanagaer.repo.UserRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class ForgotController {
	Random random = new Random();
	
	@Autowired
	private EmailService emailservice;
	
	@Autowired
	private UserRepository userrepo;
	
	@Autowired
	private BCryptPasswordEncoder bcrypt;
	
//	email id form
	@GetMapping("/forgot")
	public String openEmailForm() {
		return "forgot_email_form";
	}
	
	@PostMapping("/send-otp")
	public String sendOtp(@RequestParam("email") String email , HttpSession session){
		System.out.println("Email"+email);
//		generating otp for 6 digit
		
		int otp = random.nextInt(999999);
		
		System.out.println("otp "+otp);

//		logic to send otp to email
		String subject = "Smart Contact Manager – One-Time Password (OTP)";

		String message = "<div style='font-family: Arial, sans-serif; padding: 20px; border: 1px solid #ccc;'>"
		               + "<h2 style='color: #2c3e50;'>Hello,</h2>"
		               + "<p>Thank you for using <strong>Smart Contact Manager 2025</strong>.</p>"
		               + "<p>Your One-Time Password (OTP) for verification is:</p>"
		               + "<h1 style='color: #e74c3c; font-size: 36px;'>" + otp + "</h1>"
		               + "<p>This OTP is valid for 10 minutes. Please do not share it with anyone.</p>"
		               + "<br><p>Regards,<br><strong>Smart Contact Manager Team</strong></p>"
		               + "</div>";

		String to = email;
		boolean flag =  this.emailservice.sendEmail(subject, message, to);
		
		if(flag) {
			session.setAttribute("myotp", otp);
			session.setAttribute("email", email);
			return "verify_otp";
		}else {
			session.setAttribute("message", new Message("check your email id !!", "alert-danger"));
			return "forgot_email_form";
		}
//		return "verify_otp";
	}
	
//	verify-otp
	@PostMapping("/verify-otp")
	public String verifyotp(@RequestParam("otp") int otp , HttpSession session) {
		int myotp = (int) session.getAttribute("myotp");
		String email = (String) session.getAttribute("email");
		
		if(myotp == otp) {
			User user = this.userrepo.getByUserName(email);
			
			if(user == null) {
//				error message
				session.setAttribute("message", new Message("User Does Not Exist With This Email Id..!", "alert-danger"));
				return "forgot_email_form";
			}else {
//				send change password form
				return "change_password_form";
			}
		}else {
			session.setAttribute("message", "You have entered wrong otp..!");
			return "verify_otp";
		}
//		return null;
	}
	
//	change-password
	@PostMapping("/change-password")
	public String ChangePass(@RequestParam("newpassword") String newpassword, HttpSession session) {
	    String email = (String) session.getAttribute("email");
	    User user = this.userrepo.getByUserName(email);
	    user.setPassword(this.bcrypt.encode(newpassword));
	    this.userrepo.save(user);
	    return "redirect:login?change=password changed successfully :) ";
	}

}
