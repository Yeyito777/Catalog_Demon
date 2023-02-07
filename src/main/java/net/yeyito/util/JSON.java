package net.yeyito.util;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class JSON {
    public static HashMap<Long,List<Object>> itemBatchStringToHashMap(String contentString) {
        JSONObject contentJson = new JSONObject(contentString);
        JSONArray dataArray = contentJson.getJSONArray("data");

        HashMap<Long, List<Object>> idToLowestPrice = new HashMap<>();

        for (int i = 0; i < dataArray.length(); i++) {
            List<Object> idToInfo = new ArrayList<>();
            JSONObject item = dataArray.getJSONObject(i);
            long id = item.getLong("id");
            Long lowestPrice = null;
            String name = null;
            try {lowestPrice = item.getLong("lowestPrice");} catch (Exception ignored) {}
            try {name = item.getString("name");} catch (Exception ignored) {}

            idToInfo.add(name);
            idToInfo.add(lowestPrice);
            idToLowestPrice.put(id, idToInfo);
        }
        return idToLowestPrice;
    }

    public static List<Object> itemToInfo(String contentString) {
        List<Object> itemInfo = new ArrayList<>();
        JSONObject contentJson = new JSONObject(contentString);

        int recentAveragePrice = contentJson.getInt("recentAveragePrice");
        itemInfo.add(recentAveragePrice);

        Object originalPrice = contentJson.get("originalPrice");
        itemInfo.add(originalPrice);

        int quantitySold = contentJson.getInt("sales");
        itemInfo.add(quantitySold);

        JSONArray priceDataPoints = contentJson.getJSONArray("priceDataPoints");
        itemInfo.add(priceDataPoints);

        return itemInfo;
    }
}
