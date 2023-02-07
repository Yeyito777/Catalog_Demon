package net.yeyito.util;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

public class JSON {
    public static HashMap<Long,Long> itemBatchStringToHashMap(String contentString) {
        JSONObject contentJson = new JSONObject(contentString);
        JSONArray dataArray = contentJson.getJSONArray("data");

        HashMap<Long, Long> idToLowestPrice = new HashMap<>();

        for (int i = 0; i < dataArray.length(); i++) {
            JSONObject item = dataArray.getJSONObject(i);
            long id = item.getLong("id");
            Long lowestPrice = null;
            String name = null;
            try {lowestPrice = item.getLong("lowestPrice");} catch (Exception ignored) {}
            try {name = item.getString("name");} catch (Exception ignored) {}


            idToLowestPrice.put(id, lowestPrice);
        }
        return idToLowestPrice;
    }
}
