package bsu.fpmi.chat.xml;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import bsu.fpmi.chat.model.Message;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

public final class XMLHistoryUtil {
	private static final String STORAGE_LOCATION = System.getProperty("user.home") +  File.separator + "history.xml"; // history.xml will be located in the home directory
	private static final String MESSAGES = "messages";
	private static final String MESSAGE = "message";
	private static final String ID = "id";
	private static final String USERNAME = "username";
	private static final String TEXT = "text";
	private static final String EDIT = "edit";
	private static final String DELETE = "delete";
	private static final String TIME = "time";

	private XMLHistoryUtil() {
	}

	public static synchronized void createStorage() throws ParserConfigurationException, TransformerException {
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

		Document doc = docBuilder.newDocument();
		Element rootElement = doc.createElement(MESSAGES);
		doc.appendChild(rootElement);

		Transformer transformer = getTransformer();

		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(new File(STORAGE_LOCATION));
		transformer.transform(source, result);
	}

	public static synchronized void addData(Message mess) throws ParserConfigurationException, SAXException, IOException, TransformerException {
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		Document document = documentBuilder.parse(STORAGE_LOCATION);
		document.getDocumentElement().normalize();
		
		Element root = document.getDocumentElement(); // Root <tasks> element

		Element taskElement = document.createElement(MESSAGE);
		root.appendChild(taskElement);

		taskElement.setAttribute(ID, mess.getId());

		Element username = document.createElement(USERNAME);
		username.appendChild(document.createTextNode(mess.getUsername()));
		taskElement.appendChild(username);

		Element text = document.createElement(TEXT);
		text.appendChild(document.createTextNode(mess.getText()));
		taskElement.appendChild(text);

		Element edit = document.createElement(EDIT);
		edit.appendChild(document.createTextNode(String.valueOf(mess.isEdit())));
		taskElement.appendChild(edit);

		Element delete = document.createElement(DELETE);
		delete.appendChild(document.createTextNode(String.valueOf(mess.isDelete())));
		taskElement.appendChild(delete);

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
		Date date = new Date();

		Element time = document.createElement(TIME);
		time.appendChild(document.createTextNode((dateFormat.format(date)).toString() + "<br>" + (timeFormat.format(date)).toString()));
		taskElement.appendChild(time);

		DOMSource source = new DOMSource(document);

		Transformer transformer = getTransformer();

		StreamResult result = new StreamResult(STORAGE_LOCATION);
		transformer.transform(source, result);
	}

	public static synchronized void updateData(Message mess) throws ParserConfigurationException, SAXException, IOException, TransformerException, XPathExpressionException {
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		Document document = documentBuilder.parse(STORAGE_LOCATION);
		document.getDocumentElement().normalize();
		Node taskToUpdate = getNodeById(document, mess.getId());

		if (taskToUpdate != null) {

			NodeList childNodes = taskToUpdate.getChildNodes();

			for (int i = 0; i < childNodes.getLength(); i++) {

				Node node = childNodes.item(i);

				if (EDIT.equals(node.getNodeName())) {
					node.setTextContent(String.valueOf(mess.isEdit()));
				}

				/*if (DONE.equals(node.getNodeName())) {
					node.setTextContent(Boolean.toString(task.isDone()));
				}*/

			}

			Transformer transformer = getTransformer();

			DOMSource source = new DOMSource(document);
			StreamResult result = new StreamResult(new File(STORAGE_LOCATION));
			transformer.transform(source, result);
		} else {
			throw new NullPointerException();
		}
	}

	public static synchronized boolean doesStorageExist() {
		File file = new File(STORAGE_LOCATION);
		return file.exists();
	}

	public static synchronized List<Message> getMessages() throws SAXException, IOException, ParserConfigurationException {
		List<Message> tasks = new ArrayList<Message>();
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		Document document = documentBuilder.parse(STORAGE_LOCATION);
		document.getDocumentElement().normalize();
		Element root = document.getDocumentElement(); // Root <tasks> element
		NodeList taskList = root.getElementsByTagName(MESSAGE);
		for (int i = 0; i < taskList.getLength(); i++) {
			Element taskElement = (Element) taskList.item(i);
			String id = taskElement.getAttribute(ID);

			String username = taskElement.getElementsByTagName(USERNAME).item(0).getTextContent();
			String text = taskElement.getElementsByTagName(TEXT).item(0).getTextContent();
			boolean edit = Boolean.valueOf(taskElement.getElementsByTagName(EDIT).item(0).getTextContent());
			boolean delete = Boolean.valueOf(taskElement.getElementsByTagName(DELETE).item(0).getTextContent());
			String time = taskElement.getElementsByTagName(TIME).item(0).getTextContent();

			tasks.add(new Message(username, text, id, edit, delete, time));
		}
		return tasks;
	}

	public static synchronized int getStorageSize() throws SAXException, IOException, ParserConfigurationException {
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		Document document = documentBuilder.parse(STORAGE_LOCATION);
		document.getDocumentElement().normalize();
		Element root = document.getDocumentElement(); // Root <tasks> element
		return root.getElementsByTagName(MESSAGE).getLength();
	}

	private static Node getNodeById(Document doc, String id) throws XPathExpressionException {
		XPath xpath = XPathFactory.newInstance().newXPath();
		XPathExpression expr = xpath.compile("//" + MESSAGE + "[@id='" + id + "']");
		return (Node) expr.evaluate(doc, XPathConstants.NODE);
	}

	private static Transformer getTransformer() throws TransformerConfigurationException {
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		// Formatting XML properly
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		return transformer;
	}

}
