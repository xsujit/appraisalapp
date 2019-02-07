package com.masteknet.appraisal.controllers;

import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.masteknet.appraisal.domain.models.RegistrationForm;
import com.masteknet.appraisal.services.EmployeeService;
import com.masteknet.appraisal.services.ProjectService;
import com.masteknet.appraisal.services.UserDetailsServiceImpl;

@Controller
public class RegistrationController {
		
	@Autowired
	private ProjectService projectService;
	@Autowired
	private UserDetailsServiceImpl userService;
	@Autowired
	protected EmployeeService employeeService;

	@GetMapping("/register")
    public String getRegistrationForm(Model model) {
		
		model.addAttribute("projects", projectService.createProjectMap());
		model.addAttribute(new RegistrationForm());	
        return "register";
    }	
	
	@PostMapping("/register")
	public String registerUser(@Valid @ModelAttribute RegistrationForm registrationForm, BindingResult result, Model model, RedirectAttributes redirectAttributes) {
		
		model.addAttribute("projects", projectService.createProjectMap()); // so that drop-down values are still available in case of an error
		if (result.hasErrors()) {
			return "register";
		}
		if(employeeService.employeeIdExists(Long.parseLong(registrationForm.getEmployeeId()))) {
			result.rejectValue("employeeId", "error.registrationForm", "Employee Id already exists.");
			return "register";
		}
		if(userService.emailExists(registrationForm.getUsername())) {
			result.rejectValue("username", "error.registrationForm", "An account already exists for this email.");
			return "register";
		}
		employeeService.createBasicUser(registrationForm, projectService.getProject(registrationForm.getProjectId()));
		return "redirect:/login";
	}
}
