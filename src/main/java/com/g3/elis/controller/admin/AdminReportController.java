package com.g3.elis.controller.admin;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.g3.elis.dto.report.CoursePerformance;
import com.g3.elis.dto.report.CourseProgress;
import com.g3.elis.model.EnrolledCourse;
import com.g3.elis.model.UserLog;
import com.g3.elis.service.CourseService;
import com.g3.elis.service.EnrolledCourseService;
import com.g3.elis.service.ReportService;
import com.g3.elis.service.UserLogService;
import com.g3.elis.util.InputFileService;

@Controller
@RequestMapping("/admin")
public class AdminReportController {

	@Autowired
	private EnrolledCourseService enrolledCourseService;

	@Autowired
	private CourseService courseService;

	@Autowired
	private ReportService reportService;
	
	@Autowired 
	private UserLogService userLogService;
	
	@Autowired
	private InputFileService inputFileService;
	

	@GetMapping("/admin-report")
	public String home(@RequestParam(name= "courseId")int courseId,
						Authentication authentication, Model model) {

		List<CourseProgress> reports = reportService.generateCourseProgressReport(courseId); 
		model.addAttribute("reports", reports);
		model.addAttribute("courseId",courseId);
		model.addAttribute("enrolledCourses", enrolledCourseService.getAllEnrolledCourse().size());
		model.addAttribute("content", "admin/admin-report");
		return "/admin/admin-layout";
	}
	@GetMapping("/admin-report/generate-course-progress-report")
	public String generateReport(@RequestParam(name= "courseId")int courseId,Model model)
	{
		inputFileService.generateCourseProgressExcelReportFile(reportService.generateCourseProgressReport(courseId));
		List<CourseProgress> reports = reportService.generateCourseProgressReport(courseId); 
		model.addAttribute("reports", reports);
		model.addAttribute("courseId",courseId);
		model.addAttribute("enrolledCourses", enrolledCourseService.getAllEnrolledCourse().size());
		model.addAttribute("content", "admin/admin-report");
		return "redirect:/admin/admin-report?courseId="+courseId;
	}
}
