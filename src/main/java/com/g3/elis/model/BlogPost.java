package com.g3.elis.model;



import java.sql.Timestamp;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "blogposts")

public class BlogPost {
					@Id
					@GeneratedValue(strategy = GenerationType.IDENTITY)
					private int id;
					
					private Timestamp createdAt;
					private Timestamp updatedAt;
					private String title;
					private String description;
					
					 @Column(name = "content",  columnDefinition = "TEXT")
					  private String content;
		
					@ManyToOne
					@JoinColumn(name="user_id")
					private User users;

					public BlogPost(int id, Timestamp createdAt, Timestamp updatedAt, String title, String description,
							String content, User users) {
						super();
						this.id = id;
						this.createdAt = createdAt;
						this.updatedAt = updatedAt;
						this.title = title;
						this.description = description;
						this.content = content;
						this.users = users;
					}





					public int getId() {
						return id;
					}





					public void setId(int id) {
						this.id = id;
					}





					public Timestamp getCreatedAt() {
						return createdAt;
					}





					public void setCreatedAt(Timestamp createdAt) {
						this.createdAt = createdAt;
					}





					public Timestamp getUpdatedAt() {
						return updatedAt;
					}





					public void setUpdatedAt(Timestamp updatedAt) {
						this.updatedAt = updatedAt;
					}





					public String getTitle() {
						return title;
					}





					public void setTitle(String title) {
						this.title = title;
					}





					public String getDescription() {
						return description;
					}





					public void setDescription(String description) {
						this.description = description;
					}





					public String getContent() {
						return content;
					}





					public void setContent(String content) {
						this.content = content;
					}





					public User getUsers() {
						return users;
					}





					public void setUsers(User users) {
						this.users = users;
					}





					public BlogPost() {
						super();
		
					}


}