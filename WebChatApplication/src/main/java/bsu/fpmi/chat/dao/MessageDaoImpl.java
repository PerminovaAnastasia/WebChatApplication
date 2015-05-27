package bsu.fpmi.chat.dao;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import bsu.fpmi.chat.xml.XMLHistoryChange;
import org.apache.log4j.Logger;
import bsu.fpmi.chat.db.ConnectionManager;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;


/**
 * Created by Anastasia on 27.05.2015.
 */
public class MessageDaoImpl implements MessageDao{
    private static Logger logger = Logger.getLogger(MessageDaoImpl.class.getName());

    private static final String MESSAGES = "messages";
    private static final String TOKEN = "token";

    private static final String ID = "id";
    private static final String USERNAME = "username";
    private static final String TEXT = "text";
    private static final String EDIT = "edit";
    private static final String DELETE = "delete";
    private static final String TIME = "time";

    @Override
    public void add(JSONObject message) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = ConnectionManager.getConnection();

            preparedStatement = connection.prepareStatement("INSERT INTO messages (id, text, time, username, edit, deleted ) VALUES (?, ?, ?, ?, ?, ?)");
            preparedStatement.setString(1, (String)message.get(ID));
            preparedStatement.setString(2, (String)message.get(TEXT));
            preparedStatement.setString(3, (String)message.get(TIME));
            preparedStatement.setString(4, (String) message.get(USERNAME));
            preparedStatement.setBoolean(5, (Boolean) message.get(EDIT));
            preparedStatement.setBoolean(6, (Boolean) message.get(DELETE));

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            logger.error(e);
        } finally {
            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                } catch (SQLException e) {
                    logger.error(e);
                }
            }

            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    logger.error(e);
                }
            }
        }
    }

    @Override
    public void update(JSONObject message) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = ConnectionManager.getConnection();
            preparedStatement = connection.prepareStatement("Update messages SET text = ?, time = ?, username = ?, edit = ?, deleted = ?  WHERE id = ?");

            preparedStatement.setString(1, (String)message.get(TEXT));
            preparedStatement.setString(2, (String) message.get(TIME));
            preparedStatement.setString(3, (String)message.get(USERNAME));
            preparedStatement.setBoolean(4, (Boolean) message.get(EDIT));
            preparedStatement.setBoolean(5, (Boolean)message.get(DELETE));

            preparedStatement.setString(6, (String) message.get(ID));

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            logger.error(e);
        } finally {
            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                } catch (SQLException e) {
                    logger.error(e);
                }
            }

            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    logger.error(e);
                }
            }
        }
    }

    @Override
    public String get(int index){

        JSONArray jsonArray = new JSONArray();
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        JSONObject temp= new JSONObject();

        try {
            ArrayList<String> tempId = XMLHistoryChange.getIds(index);
            connection = ConnectionManager.getConnection();

            for (int i = 0; i < tempId.size(); i++)
            {
                preparedStatement = connection.prepareStatement("SELECT * FROM messages WHERE id = ?");
                preparedStatement.setString(1, (String) tempId.get(i));
                resultSet = preparedStatement.executeQuery();
                resultSet.next();

                JSONObject jsonObject = new JSONObject();

                String id = resultSet.getString(ID);
                String text = resultSet.getString(TEXT);
                String time = resultSet.getString(TIME);
                String username = resultSet.getString(USERNAME);
                boolean edit = resultSet.getBoolean(EDIT);
                boolean delete = resultSet.getBoolean("deleted");

                jsonObject.put(EDIT, Boolean.valueOf(edit));
                jsonObject.put("delete", Boolean.valueOf(delete));
                jsonObject.put(TEXT, text);
                jsonObject.put(ID, id);
                jsonObject.put(USERNAME, username);
                jsonObject.put(TIME, time);

                jsonArray.add(jsonObject);
            }

            temp.put(MESSAGES,jsonArray);
            temp.put(TOKEN, XMLHistoryChange.getStorageSize());

        } catch (SQLException | SAXException | IOException | ParserConfigurationException e) {
            logger.error(e);
        } finally {
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    logger.error(e);
                }
            }
            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                } catch (SQLException e) {
                    logger.error(e);
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    logger.error(e);
                }
            }
        }

        return temp.toString();
    }

    @Override
    public void delete(String id) {

        Connection connection = ConnectionManager.getConnection();
        PreparedStatement preparedStatement = null;

        try {
            connection = ConnectionManager.getConnection();
            preparedStatement = connection.prepareStatement("Update messages SET text = ?, edit = ?, deleted = ? WHERE id = ?");

            preparedStatement.setString(1, "");
            preparedStatement.setBoolean(2, false);
            preparedStatement.setBoolean(3, true);
            preparedStatement.setString(4, id);
            preparedStatement.executeUpdate();

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            logger.error(e);
        } finally {
            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                } catch (SQLException e) {
                    logger.error(e);
                }
            }

            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    logger.error(e);
                }
            }
        }

       // throw new UnsupportedOperationException();
    }

}
