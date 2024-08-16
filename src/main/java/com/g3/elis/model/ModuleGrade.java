package com.g3.elis.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "achievements")
public class ModuleGrade {

	

	             @Id
	             @GeneratedValue(strategy = GenerationType.IDENTITY)
	             private int id;
	             private String grade;


				@ManyToOne
				@JoinColumn(name="user_id")
				private User users;
				
				@ManyToOne
				@JoinColumn(name="course_id")
				private Course courses;
				
				@OneToOne
				@JoinColumn(name="course_module_id")
				private CourseModule course_modules;

				public int getId() {
					return id;
				}

				public void setId(int id) {
					this.id = id;
				}

				public String getGrade() {
					return grade;
				}

				public void setGrade(String grade) {
					this.grade = grade;
				}

				public User getUsers() {
					return users;
				}

				public void setUsers(User users) {
					this.users = users;
				}

				public Course getCourses() {
					return courses;
				}

				public void setCourses(Course courses) {
					this.courses = courses;
				}

				public CourseModule getCourse_modules() {
					return course_modules;
				}

				public void setCourse_modules(CourseModule course_modules) {
					this.course_modules = course_modules;
				}

				public ModuleGrade(int id, String grade, User users, Course courses, CourseModule course_modules) {
					super();
					this.id = id;
					this.grade = grade;
					this.users = users;
					this.courses = courses;
					this.course_modules = course_modules;
				}

				public ModuleGrade() {
					super();
					// TODO Auto-generated constructor stub
				}
				
				
				
				
				




		
}
