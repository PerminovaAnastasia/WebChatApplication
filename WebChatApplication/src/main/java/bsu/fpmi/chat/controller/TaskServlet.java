package bsu.fpmi.chat.controller;

import static bsu.fpmi.chat.util.MessageUtil.MESSAGES;
import static bsu.fpmi.chat.util.MessageUtil.TOKEN;
import static bsu.fpmi.chat.util.MessageUtil.getIndex;
import static bsu.fpmi.chat.util.MessageUtil.getToken;
import static bsu.fpmi.chat.util.MessageUtil.jsonToMessage;
import static bsu.fpmi.chat.util.MessageUtil.stringToJson;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.log4j.Logger;
import bsu.fpmi.chat.model.Message;
import bsu.fpmi.chat.model.MessageStorage;
import bsu.fpmi.chat.xml.XMLHistoryUtil;
import bsu.fpmi.chat.util.ServletUtil;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.xml.sax.SAXException;

@WebServlet("/chat")
public class TaskServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static Logger logger = Logger.getLogger(TaskServlet.class.getName());

	@Override
	public void init() throws ServletException {
		try {
			loadHistory();
		} catch (SAXException | IOException | ParserConfigurationException | TransformerException e) {
			logger.error(e);
		}
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		logger.info("doGet");
		String token = request.getParameter(TOKEN);
		logger.info("Token " + token);

		if (token != null && !"".equals(token)) {
			int index = getIndex(token);
			logger.info("Index " + index);
			String tasks = formResponse(index);
			response.setContentType(ServletUtil.APPLICATION_JSON);
			PrintWriter out = response.getWriter();
			out.print(tasks);
			out.flush();

		} else {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "'token' parameter needed");
		}
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		logger.info("doPost");
		String data = ServletUtil.getMessageBody(request);
		logger.info(data);
		try {
			JSONObject json = stringToJson(data);
			Message mess = jsonToMessage(json);
			MessageStorage.addTask(mess);
			XMLHistoryUtil.addData(mess);
			response.setStatus(HttpServletResponse.SC_OK);

			System.out.println(mess.getTime() + " "+ mess.getUsername()+" : "+mess.getText());
		} catch (ParseException | ParserConfigurationException | SAXException | TransformerException e) {
			logger.error(e);
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
		}
	}

//	@Override
//	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//		logger.info("doPut");
//		String data = ServletUtil.getMessageBody(request);
//		logger.info(data);
//		try {
//			JSONObject json = stringToJson(data);
//			Message mess = jsonToMessage(json);
//			String id = mess.getId();
//			Message messToUpdate = MessageStorage.getMessageById(id);
//			if (messToUpdate != null) {
//				messToUpdate.setEdit(mess.isEdit());
//				messToUpdate.setDelete(mess.isDelete());
//				messToUpdate.setText(mess.getText());
//				messToUpdate.setTime(mess.getTime());
//
//				XMLHistoryUtil.updateData(messToUpdate);
//				/*messToUpdate.setDescription(task.getDescription());
//				messToUpdate.setDone(task.isDone());
//				XMLHistoryUtil.updateData(taskToUpdate);*/
//				response.setStatus(HttpServletResponse.SC_OK);
//			} else {
//				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Task does not exist");
//			}
//		} catch (ParseException | ParserConfigurationException | SAXException | TransformerException | XPathExpressionException e) {
//			logger.error(e);
//			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
//		}
//	}

	private String formResponse(int index) {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put(MESSAGES, MessageStorage.getSubMessagesByIndex(index));
		jsonObject.put(TOKEN, getToken(MessageStorage.getSize()));
		return jsonObject.toJSONString();
	}

	private void loadHistory() throws SAXException, IOException, ParserConfigurationException, TransformerException  {
		if (XMLHistoryUtil.doesStorageExist()) {
			MessageStorage.addAll(XMLHistoryUtil.getMessages());
		} else {
			XMLHistoryUtil.createStorage();
		}
	}
}
