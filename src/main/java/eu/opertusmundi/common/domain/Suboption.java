package eu.opertusmundi.common.domain;

public class Suboption {

	Integer id;
	Integer option;
	String body;
	

	public Suboption(int id, int option, String body) {
		this.id = id;
		this.option = option;
		this.body = body;
		
	}
	
	public Integer getId() {
		return id;
	}
	
	public Integer getOption() {
		return option;
	}
	
	public String getBody() {
		return body;
	}
}
