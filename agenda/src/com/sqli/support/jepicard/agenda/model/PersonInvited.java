package com.sqli.support.jepicard.agenda.model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class PersonInvited extends Person{
	private Answer answer=Answer.NOT_ANSWERED;

	public PersonInvited() {
		super();
	}

	public PersonInvited(String firstName, String lastName) {
		super(firstName, lastName);
	}

	public Answer getAnswer() {
		return answer;
	}

	public void setAnswer(Answer answer) {
		this.answer=answer;
	}
}
