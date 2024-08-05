package com.g3.elis.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name="answers")
public class Answer {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
	
	@Column(name = "answer_title", nullable = false, length = 255)
    private String answerTitle;
	
	@Column(name = "correct_status", nullable = false)
    private boolean correctStatus;
	
	@ManyToOne
	@JoinColumn(name ="question_id", nullable = false)
	private Question questions;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getAnswerTitle() {
		return answerTitle;
	}

	public void setAnswerTitle(String answerTitle) {
		this.answerTitle = answerTitle;
	}

	public boolean isCorrectStatus() {
		return correctStatus;
	}

	public void setCorrectStatus(boolean correctStatus) {
		this.correctStatus = correctStatus;
	}

	public Question getQuestion() {
		return questions;
	}

	public void setQuestion(Question question) {
		this.questions = question;
	}

	public Answer() {
		super();
	}

	public Answer(int id, String answerTitle, boolean correctStatus, Question question) {
		super();
		this.id = id;
		this.answerTitle = answerTitle;
		this.correctStatus = correctStatus;
		this.questions = question;
	}
		

}