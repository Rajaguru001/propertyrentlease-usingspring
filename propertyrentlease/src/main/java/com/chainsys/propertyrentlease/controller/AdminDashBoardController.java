package com.chainsys.propertyrentlease.controller;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.chainsys.propertyrentlease.dao.PropertyRentLeaseImpl;
import com.chainsys.propertyrentlease.model.Comment;
import com.chainsys.propertyrentlease.model.PropertyImage;
import com.chainsys.propertyrentlease.model.SellerPropertyForm;

@Controller
public class AdminDashBoardController {

	@Autowired
	PropertyRentLeaseImpl propertyimpl;

	@PostMapping("/admindashboard")
	public String adminapproval(@RequestParam("propertyId") int propertyid) {

		propertyimpl.approveproperty(propertyid);

		return "admindashboard";

	}

	@GetMapping("/buyerview")
	public String showApprovedProperties(Model model) {
		List<SellerPropertyForm> approvedProperties = propertyimpl.getApprovedProperties();
		List<Comment> comments21 = new ArrayList<>();

		for (SellerPropertyForm sp : approvedProperties) {

			List<Comment> comments = propertyimpl.getcomment(sp.getPropertyId());
			comments21.addAll(comments);
			  List<PropertyImage> propertyImages = propertyimpl.getPropertyImage(sp.getPropertyId());
	            List<String> base64Images = new ArrayList<>();

	            for (PropertyImage img : propertyImages) {
	                byte[] imageData = img.getImage();
	                if (imageData != null && imageData.length > 0) {
	                    String base64Image = Base64.getEncoder().encodeToString(imageData);
	                    base64Images.add(base64Image);
	                    sp.setPropertyImages(base64Images);
	                }
	            }

		}
		
		model.addAttribute("comments", comments21);

		model.addAttribute("approvedProperties", approvedProperties);
		return "buyerpropertyview";
	}

}
