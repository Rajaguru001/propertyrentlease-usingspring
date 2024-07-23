package com.chainsys.propertyrentlease.controller;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.chainsys.propertyrentlease.dao.PropertyRentLeaseImpl;
import com.chainsys.propertyrentlease.model.PropertyImage;
import com.chainsys.propertyrentlease.model.SellerDashBoardRequest;
import com.chainsys.propertyrentlease.model.SellerProperty;
import com.chainsys.propertyrentlease.model.SellerPropertyForm;
import com.chainsys.propertyrentlease.model.Users;
import com.chainsys.propertyrentlease.validation.PropertyRentLeaseException;
import com.chainsys.propertyrentlease.validation.PropertyRentLeaseValidation;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class UserController {

	private final PropertyRentLeaseImpl propertyimpl;

	@Autowired
	public UserController(PropertyRentLeaseImpl propertyimpl) {
		this.propertyimpl = propertyimpl;
	}

	@Autowired
	PropertyRentLeaseValidation propertyvalidation;

	@RequestMapping("/")
	public String home() {
		return "homepage";
	}

	@RequestMapping("/register")
	public String registerpage() {
		return "register";
	}

	@PostMapping("/registeruser")
	public String register(@RequestParam("name") String name, @RequestParam("password") String password,
			@RequestParam("email") String email, @RequestParam("phonenumber") String phonenumber,
			RedirectAttributes redirectAttributes, Model model) throws PropertyRentLeaseException {
		Users users = new Users();
		while (true) {
			try {
				if (!propertyvalidation.validateUsername(name)) {
					model.addAttribute("error", "Invalid username");
					return "register";

				}
			} catch (PropertyRentLeaseException ex) {
				System.out.println("invalid username");
				System.out.println("the expcetion is:" + ex);
				return "register";

			}
			break;

		}
		if (!propertyvalidation.passwordsMatch(password)) {
			model.addAttribute("error", "Invalid password");
			return "register";
		}
		if (!propertyvalidation.validateEmail(email)) {
			model.addAttribute("error", "Invalid email");
			return "register";
		}
		if (!propertyvalidation.validateMobile(phonenumber)) {
			model.addAttribute("error", "Invalid phonenumber");
			return "register";
		}
		users.setUsername(name);
		users.setPassword(password);
		users.setEmail(email);
		users.setPhonenumber(phonenumber);

		if (propertyimpl.insert(users)) {
			return "login";
		} else {
			redirectAttributes.addFlashAttribute("alert", "You already have an account.");
			return "login";
		}
	}

	@GetMapping("/login")
	public String user() {
		return "login";
	}

	@PostMapping("/login")
	public String login(HttpSession session, @RequestParam("email") String email,
			@RequestParam("password") String password, RedirectAttributes redirectAttributes, Model model) {
		Users user = new Users();
		if (!propertyvalidation.validateEmail(email)) {
			model.addAttribute("error", "Invalid email");
			return "register";
		}
		if (!propertyvalidation.passwordsMatch(password)) {
			model.addAttribute("error", "Invalid password");
			return "register";
		}
		user.setEmail(email);
		user.setPassword(password);
		if (propertyimpl.insertLogin(user)) {

			Users users = propertyimpl.getUserIdByEmail(user);
			if (users == null) {
				redirectAttributes.addFlashAttribute("error", "Invalid email or password.");
				return "register";
			}

			session.setAttribute("user", users);

			Users adminLoginCheck = propertyimpl.adminlogincheck(users);
			//if (adminLoginCheck != null) {
				if (user.getEmail().matches("\\b[A-Za-z0-9._%+-]+@eliterental\\.com\\b")
						&& user.getPassword().matches("Raju@123")) {
//					SellerPropertyForm seller = new SellerPropertyForm();
//					List<SellerPropertyForm> propertyDetailsList = propertyimpl.getPropertyDetails();
//					if (propertyDetailsList != null) {
//						for (SellerPropertyForm property : propertyDetailsList) {
//							List<PropertyImage> propertyImages = propertyimpl
//									.getPropertyImage(property.getPropertyId());
//							
//						}
//						// model.addAttribute("propertyDetailsList", propertyDetailsList);
						return "dashboard";
					}

			//	} 
				else {
					return "homepage";
				}
			}

		
		return "register";
	}

	@PostMapping("/postproperty")
	public String postProperty(HttpSession session, RedirectAttributes redirectAttributes, Model model) {
		Users loggedInUser = (Users) session.getAttribute("user");
		if (loggedInUser != null) {

			Users ownerId = propertyimpl.checkseller(loggedInUser);

			if (ownerId != null) {

				List<SellerDashBoardRequest> sellerdashboard = propertyimpl.sellerdashboard(ownerId.getUserid());
				model.addAttribute("sellerdashboard", sellerdashboard);
				return "sellerdashboard";

			} else {

				return "postproperty";
			}

		} else {

			return "login";
		}
	}

	@PostMapping("/logout")
	public String userLogout(HttpSession session, HttpServletRequest request) {
		session = request.getSession(false);
		if (session != null) {
			session.invalidate();
		}

		return "homepage";
	}

	@GetMapping("/dashboard")
	public String dashboard(Model model) {
	    List<SellerPropertyForm> propertyDetailsList = propertyimpl.getPropertyDetails();

	    if (propertyDetailsList != null) {
	        for (SellerPropertyForm property : propertyDetailsList) {
	            List<PropertyImage> propertyImages = propertyimpl.getPropertyImage(property.getPropertyId());
	            List<String> base64Images = new ArrayList<>();

	            for (PropertyImage img : propertyImages) {
	                byte[] imageData = img.getImage();
	                if (imageData != null && imageData.length > 0) {
	                    String base64Image = Base64.getEncoder().encodeToString(imageData);
	                    base64Images.add(base64Image);
	                }
	            }
	            property.setPropertyImages(base64Images);
	        }
	        model.addAttribute("propertyDetailsList", propertyDetailsList);
	    }

	    return "admindashboard";
	}
}
