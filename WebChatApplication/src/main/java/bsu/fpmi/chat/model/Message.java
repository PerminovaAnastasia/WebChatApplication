package bsu.fpmi.chat.model;

import org.json.simple.JSONAware;
import org.json.simple.JSONObject;

import java.util.UUID;

public class Message implements JSONAware {

	private String username;
	private String text;
	private String id;
	private boolean edit;
	private boolean delete;
	private String time;
	public Message() {
		username="";
		text="";
		id= UUID.randomUUID().toString();
		edit=false;
		delete=false;
		time = "";
	}
	//
	public Message(String name, String mess, String idd, Boolean editt, Boolean del, String ttime) {
		username=name;
		text = mess;
		id = UUID.randomUUID().toString();
		edit = editt;
		delete = del;
		time = ttime;
	}
	public static Message parse(Object obj){
		Message temp=new Message();
		temp.username=(String)((JSONObject)obj).get("username");
		temp.text=(String)((JSONObject)obj).get("text");
		temp.id= (((JSONObject) obj).get("id")).toString();
		temp.edit=(Boolean)(((JSONObject) obj).get("edit"));
		temp.delete=(Boolean)(((JSONObject) obj).get("delete"));
		temp.time = (String)((JSONObject)obj).get("time");
		return temp;
	}
	@Override
	public String toJSONString(){
		JSONObject obj = new JSONObject();
		obj.put("username", username);
		obj.put("text", text);
		obj.put("id", id);
		obj.put("edit", edit);
		obj.put("delete", delete);
		obj.put("time", time);
		return obj.toString();
	}
	public String toString(){
		return username+" : "+text;
	}

	public void deleteMessage()
	{
		delete = true;
		text = "The message has been deleted";
		edit = false;
	}

	public void redactMessage(String mess){
		edit = true;
		text = mess;
	}

	public void setId(String idd){ id=idd; }
	public void setEdit(Boolean editt){
		edit = editt;
	}
	public void setDelete(Boolean del){
		delete = del;
	}
	public void setText(String messText){
		text = messText;
	}
	public void setUsername(String name){
		username = name;
	}
	public void setTime(String ttime){
		time = ttime;
	}

	public String getId(){
		return id;
	}
	public boolean isEdit(){
		return edit;
	}
	public boolean isDelete(){
		return delete;
	}
	public String getText(){
		return text;
	}

	public String getUsername(){
		return username;
	}
	public String getTime(){
		return time;
	}
}
