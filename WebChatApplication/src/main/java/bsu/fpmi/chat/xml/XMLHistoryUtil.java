package bsu.fpmi.chat.xml;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

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

import bsu.fpmi.chat.util.MessageUtil;
import jdk.internal.org.objectweb.asm.tree.analysis.Value;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
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
	private static final String TOKEN = "token";

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

	public static synchronized void addData(JSONObject mess) throws ParserConfigurationException, SAXException, IOException, TransformerException {
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		Document document = documentBuilder.parse(STORAGE_LOCATION);
		document.getDocumentElement().normalize();
		
		Element root = document.getDocumentElement(); // Root <tasks> element

		Element taskElement = document.createElement(MESSAGE);
		root.appendChild(taskElement);

		//String tempID = UUID.randomUUID().toString();
		/*Element tempId = document.createElement(ID);
		tempId.appendChild(document.createTextNode((String)mess.get(ID)));*/
		taskElement.setAttribute(ID, mess.get(ID).toString());

		XMLHistoryChange.addData(mess.get(ID).toString());

		Element username = document.createElement(USERNAME);
		username.appendChild(document.createTextNode((String)mess.get(USERNAME)));
		taskElement.appendChild(username);

		Element text = document.createElement(TEXT);
		text.appendChild(document.createTextNode((String)mess.get(TEXT)));
		taskElement.appendChild(text);

		Element edit = document.createElement(EDIT);
		edit.appendChild(document.createTextNode(String.valueOf(mess.get(EDIT))));
		taskElement.appendChild(edit);

		Element delete = document.createElement(DELETE);
		delete.appendChild(document.createTextNode(String.valueOf(mess.get(DELETE))));
		//delete.appendChild(document.createTextNode((String)(mess.get(DELETE)))); for error jsp
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

	public static synchronized void updateData(JSONObject json) throws ParserConfigurationException, SAXException, IOException, TransformerException, XPathExpressionException {
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		Document document = documentBuilder.parse(STORAGE_LOCATION);
		document.getDocumentElement().normalize();

		Node taskToUpdate = getNodeById(document, json.get(ID).toString());
		XMLHistoryChange.addData(json.get(ID).toString());

		if (taskToUpdate != null) {

			NodeList childNodes = taskToUpdate.getChildNodes();

			for (int i = 0; i < childNodes.getLength(); i++) {

				Node node = childNodes.item(i);

				if (EDIT.equals(node.getNodeName())) {
					node.setTextContent(String.valueOf(String.valueOf(json.get(EDIT))));
				}

                if (DELETE.equals(node.getNodeName())) {
                    node.setTextContent(String.valueOf(String.valueOf(json.get(DELETE))));
                }
                if (TEXT.equals(node.getNodeName())) {
                    node.setTextContent(String.valueOf(String.valueOf(json.get(TEXT))));
                }

			}

			Transformer transformer = getTransformer();

			DOMSource source = new DOMSource(document);
			StreamResult result = new StreamResult(new File(STORAGE_LOCATION));
			transformer.transform(source, result);
		} else {
			throw new NullPointerException();
		}
	}

	public static synchronized void deleteData(String idDel) throws ParserConfigurationException, SAXException, IOException, TransformerException, XPathExpressionException {
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		Document document = documentBuilder.parse(STORAGE_LOCATION);
		document.getDocumentElement().normalize();

		Node taskToUpdate = getNodeById(document, idDel);
		XMLHistoryChange.addData(idDel);

		if (taskToUpdate != null) {

			NodeList childNodes = taskToUpdate.getChildNodes();

			for (int i = 0; i < childNodes.getLength(); i++) {

				Node node = childNodes.item(i);

				if (DELETE.equals(node.getNodeName())) {
					node.setTextContent(String.valueOf(String.valueOf(true)));
				}
				if (TEXT.equals(node.getNodeName())) {
					node.setTextContent("The message was deleted");
				}

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

	public static synchronized String getMessages(int index) throws SAXException, IOException, ParserConfigurationException,  XPathExpressionException {

		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		Document document = documentBuilder.parse(STORAGE_LOCATION);
		document.getDocumentElement().normalize();
		Element root = document.getDocumentElement(); // Root <tasks> element
		NodeList taskList = root.getElementsByTagName(MESSAGE);

		JSONArray jsonArray = new JSONArray();

        ArrayList<String> tempId = XMLHistoryChange.getIds(index);

		for (int i = 0; i < tempId.size(); i++) {

            Node taskToUpdate = getNodeById(document, tempId.get(i));
            JSONObject jsonObject = new JSONObject();

            if (taskToUpdate != null) {

                NodeList childNodes = taskToUpdate.getChildNodes();

                for (int j = 0; j < childNodes.getLength(); j++) {

                    Node node = childNodes.item(j);

                    if (EDIT.equals(node.getNodeName())) {
                        jsonObject.put(EDIT, Boolean.valueOf(node.getTextContent()));
                    }
                    if (DELETE.equals(node.getNodeName())) {
                        jsonObject.put(DELETE, Boolean.valueOf(node.getTextContent()));
                    }
                    if (TEXT.equals(node.getNodeName())) {
                        jsonObject.put(TEXT, node.getTextContent());
                    }
                    if (USERNAME.equals(node.getNodeName())) {
                        jsonObject.put(USERNAME, node.getTextContent());
                    }
                    if (TIME.equals(node.getNodeName())) {
                        jsonObject.put(TIME, node.getTextContent());
                    }

                }
            }
			jsonObject.put(ID, tempId.get(i));
			jsonArray.add(jsonObject);
		}
		JSONObject temp= new JSONObject();
		temp.put(MESSAGES,jsonArray);
		temp.put(TOKEN,XMLHistoryChange.getStorageSize());
		return temp.toString();
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
