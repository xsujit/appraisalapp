package com.masteknet.appraisal.controllers;

import java.util.ArrayList;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.masteknet.appraisal.domain.models.Team;
import com.masteknet.appraisal.entities.Appraisal;
import com.masteknet.appraisal.entities.AppraisalCategory;
import com.masteknet.appraisal.entities.Comment;
import com.masteknet.appraisal.entities.Employee;
import com.masteknet.appraisal.exceptions.AppraisalNotFoundException;
import com.masteknet.appraisal.services.AppraisalService;
import com.masteknet.appraisal.services.EmployeeService;
import com.masteknet.appraisal.services.TeamService;

@Controller
public class TeamController {
	
	@Autowired
	private TeamService teamService;
	@Autowired
	private AppraisalService appraisalService;
	@Autowired
	protected EmployeeService employeeService;
	
	private Employee getEmployee(long id) { // private
		return employeeService.getEmployee(id);
	}
		
	@GetMapping("/team")
	public String getTeam(Model model, HttpSession session) {
		ArrayList<Team> teamList = teamService.getTeam(
				(AppraisalCategory) session.getAttribute("appraisalCategory"), (Employee) session.getAttribute("loggedInEmployee"));
		if (teamList != null) {
			model.addAttribute("teamList", teamList);
			return "team";
		}
		return "redirect:/error?message=Your+team+could+not+be+displayed.+Please+contact+support.";
	}
	
	@GetMapping("/team/{employeeId}")
	public String getAppraisalByEmployee(Model model, @PathVariable String employeeId, HttpSession session) {
		
		Employee me = (Employee) session.getAttribute("loggedInEmployee");
		Appraisal appraisal = appraisalService.getAppraisal(getEmployee(Long.parseLong(employeeId)), (AppraisalCategory) session.getAttribute("appraisalCategory"));
		if(appraisal == null || !appraisal.isSignedOff()) {
			throw new AppraisalNotFoundException(employeeId);
		} else {
			model.addAttribute("employee", getEmployee(Long.parseLong(employeeId)));
			model.addAttribute("appraisal", appraisal);
			model.addAttribute("comments", teamService.getComments(appraisal));
			model.addAttribute("comment", new Comment());
			model.addAttribute("eligible", (teamService.hasVoted(appraisal, me)	|| teamService.selfVote(appraisal, me) ? false : true));
			return "team-view";
		}
	}
	
	@GetMapping("/team/{employeeId}/vote-a-plus")
	public String saveVote(@PathVariable String employeeId, HttpSession session) {
		
		Employee me = (Employee) session.getAttribute("loggedInEmployee");
		Appraisal appraisal = appraisalService.getAppraisal(getEmployee(Long.parseLong(employeeId)), (AppraisalCategory) session.getAttribute("appraisalCategory"));
		if(appraisal == null) {
			throw new AppraisalNotFoundException(employeeId);
		}
		if(teamService.selfVote(appraisal, me) || teamService.hasVoted(appraisal, me)) {
			return "redirect:/team/{employeeId}?error=Already+voted+or+self+vote.";
		} 
		try {
			teamService.saveVote(appraisal, me);
		} catch (DataAccessException dae) {
			return "redirect:/team/{employeeId}?error=Unable+to+save+your+vote.+Please+contact+support.";
		}
		return "redirect:/team/{employeeId}?success=Vote+registered+successfully.";
	}
	
	@PostMapping("/team/{employeeId}/comment")
	public String saveComment(@Valid @ModelAttribute Comment comment, BindingResult result, @PathVariable String employeeId, Model model, HttpSession session) {
		
		Employee me = (Employee) session.getAttribute("loggedInEmployee");
		Appraisal appraisal = appraisalService.getAppraisal(getEmployee(Long.parseLong(employeeId)), (AppraisalCategory) session.getAttribute("appraisalCategory"));
		if(appraisal == null) {
			throw new AppraisalNotFoundException(employeeId);
		}
		if (result.hasErrors()) {
				model.addAttribute("employee", getEmployee(Long.parseLong(employeeId)));
				model.addAttribute("appraisal", appraisal);
				model.addAttribute("comments", teamService.getComments(appraisal));
				model.addAttribute("eligible", teamService.selfVote(appraisal, me) ? false : true);
				return "team-view";
		}
		try {
			teamService.saveComment(comment, appraisal, me);	
		} catch (DataAccessException dae) {
			return "redirect:/team/{employeeId}?error=Your+comment+could+not+be+posted.+Please+contact+support.";
		}
		return "redirect:/team/{employeeId}?success=Your+comment+was+posted+successfully.";
	}
	
}
