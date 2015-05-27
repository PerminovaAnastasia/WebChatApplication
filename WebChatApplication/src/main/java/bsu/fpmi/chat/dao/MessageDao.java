package bsu.fpmi.chat.dao;

import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * Created by Anastasia on 27.05.2015.
 */
public interface MessageDao {
    void add(JSONObject message);

    void update(JSONObject message);

    void delete(String id);

    String get(int index);
}
