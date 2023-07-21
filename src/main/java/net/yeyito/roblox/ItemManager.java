package net.yeyito.roblox;

import net.yeyito.Main;
import net.yeyito.VirtualBrowser;
import net.yeyito.connections.DiscordBot;
import net.yeyito.util.StringFilter;
import net.yeyito.util.TextFile;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import javax.annotation.Nullable;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.awt.event.InputEvent;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ItemManager {
    static List<Long> boughtIDs = new ArrayList<>();
    public static void buyItem(long id, @Nullable JSONArray price_data_points) {
        try {
            Main.discordBot.sendMessageOnRegisteredChannels("Buying item: " + id, 0);
            System.out.println("Buying item: " + id);
            String expected_price = "";
            String seller_id = "";
            String expected_currency = "";
            String productID = "";
            String token = "";
            String user_asset_id = "";
            String user_robux = "";

            VirtualBrowser virtualBrowser = new VirtualBrowser();
            String getItem = (String) virtualBrowser.curlToOpenWebsite("curl 'https://www.roblox.com/catalog/" + id + "' \\\n" +
                    "  -H 'cookie: " + Main.secCookie + "' \\\n").get("response");

            String[] searches = {"data-product-id=", "data-token=", "data-expected-price=", "data-expected-seller-id=", "data-expected-currency=", "data-lowest-private-sale-userasset-id=", "data-user-balance-robux="};

            for (String search : searches) {
                int index = getItem.indexOf(search);
                if (index != -1) {
                    String sub = getItem.substring(index + search.length() + 1);
                    Pattern pattern = search.equals("data-product-id=") ? Pattern.compile("\\d+") : Pattern.compile("[^\"\\s]*");
                    Matcher matcher = pattern.matcher(sub);

                    if (matcher.find()) {
                        String result = matcher.group();
                        switch (search) {
                            case "data-product-id=":
                                productID = result;
                                break;
                            case "data-token=":
                                token = result;
                                break;
                            case "data-expected-price=":
                                expected_price = result;
                                break;
                            case "data-expected-seller-id=":
                                seller_id = result;
                                break;
                            case "data-expected-currency=":
                                expected_currency = result;
                                break;
                            case "data-lowest-private-sale-userasset-id=":
                                user_asset_id = result;
                                break;
                            case "data-user-balance-robux=":
                                user_robux = result;
                                break;
                        }
                    }
                }
            }

            System.out.println("Product ID: " + productID);
            System.out.println("Token: " + token);
            System.out.println("Expected Price: " + expected_price);
            System.out.println("Seller ID: " + seller_id);
            System.out.println("Expected Currency: " + expected_currency);
            System.out.println("User Asset ID: " + user_asset_id);
            System.out.println("User Robux: " + user_robux);

            if (buyModel(Integer.parseInt(user_robux), Integer.parseInt(expected_price), id, price_data_points)) {
                JSONObject body = new JSONObject();
                body.put("expectedCurrency", expected_currency);
                body.put("expectedPrice", expected_price);
                body.put("expectedSellerId", seller_id);
                body.put("userAssetId", user_asset_id);

                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(new URI("https://economy.roblox.com/v1/purchases/products/" + productID))
                        .header("authority", "economy.roblox.com")
                        .header("Content-Type", "application/json; charset=utf-8")
                        .header("cookie", Main.secCookie)
                        .header("x-csrf-token", token)
                        .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                        .build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                System.out.println(response.statusCode());
                System.out.println(response.body());
                if (response.statusCode() == 200) {
                    Main.discordBot.sendMessageOnRegisteredChannels("Successfully bought item: " + id, 0);
                }
            } else {
                System.err.println("Item failed buyModel: " + id + " Current price: " + expected_price);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static boolean buyModel(int userRobux, int currentPrice, long itemID, @Nullable JSONArray priceDataPointsArray) throws IOException {
        if (currentPrice < 21) {return true;}
        if (boughtIDs.contains(itemID)) {return false;}
        if (currentPrice < userRobux/3) {
            VirtualBrowser virtualBrowser = new VirtualBrowser();

            if (priceDataPointsArray == null) { // This should only happen if an item's price was detected to be < 21 and changed on the way here.
                String priceDataPoints = (String) virtualBrowser.openWebsite("https://economy.roblox.com/v1/assets/" + itemID + "/resale-data", "GET", null, null, null, false, false).get("response");
                JSONObject json = new JSONObject(priceDataPoints);
                priceDataPointsArray = json.getJSONArray("priceDataPoints");
            }

            Double average30 = calculateAveragePriceLastDays(priceDataPointsArray, 30);
            Double average90 = calculateAveragePriceLastDays(priceDataPointsArray, 90);
            Double average180 = calculateAveragePriceLastDays(priceDataPointsArray, 180);
            Double variance = calculateAverageVariance(priceDataPointsArray);
            System.out.println("Average Variance: " + variance);
            System.out.println("Average Price Last 30 Days: " + average30);
            System.out.println("Average Price Last 90 Days: " + average90);
            System.out.println("Average Price Last 180 Days: " + average180);

            if (average30 == null || average90 == null || average180 == null || variance == null) {return false;}
            System.out.println("Passed Check #1");
            if (average30/2*(1-variance) < currentPrice || average90/2*(1-variance) < currentPrice || average180/2*(1-variance) < currentPrice) {return false;}
            System.out.println("Passed Check #2");
            if (currentPrice > average30/2 || currentPrice > average90/2 || currentPrice > average180/2) {return false;}
            System.out.println("Passed Check #3");
            if (activityInDays(priceDataPointsArray,30) <= 3) {return false;}
            System.out.println("Passed Check #4");
            boughtIDs.add(itemID);
            return true;
        } else {
            return false;
        }
    }

    public static Double calculateAverageVariance(JSONArray priceDataPointsArray) {
        int n = priceDataPointsArray.length();
        if (n == 0) return null;

        double sum = 0, indVariance;
        for (int i = 1; i < n; i++) {
            double variance = (double) (priceDataPointsArray.getJSONObject(i).getInt("value") - priceDataPointsArray.getJSONObject(i - 1).getInt("value")) / (priceDataPointsArray.getJSONObject(i).getInt("value") + priceDataPointsArray.getJSONObject(i-1).getInt("value"));
            sum += Math.abs(variance);
        }

        indVariance = sum / n;
        return indVariance;
    }

    public static Double calculateAveragePriceLastDays(JSONArray priceDataPointsArray, int days) {
        LocalDate now = LocalDate.now();
        LocalDate pastDate = now.minusDays(days);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        double sum = 0;
        int count = 0;
        for (int i = 0; i < priceDataPointsArray.length(); i++) {
            JSONObject dataPoint = priceDataPointsArray.getJSONObject(i);
            LocalDate date = LocalDate.parse(dataPoint.getString("date").substring(0, 10), formatter);
            if (!date.isBefore(pastDate)) {
                sum += dataPoint.getInt("value");
                count++;
            }
        }

        return (count == 0) ? null : (sum / count);
    }

    public static int activityInDays(JSONArray priceDataPointsArray, int days) {
        LocalDate now = LocalDate.now();
        LocalDate pastDate = now.minusDays(days);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        int count = 0;
        for (int i = 0; i < priceDataPointsArray.length(); i++) {
            JSONObject dataPoint = priceDataPointsArray.getJSONObject(i);
            LocalDate date = LocalDate.parse(dataPoint.getString("date").substring(0, 10), formatter);
            if (!date.isBefore(pastDate)) {
                count++;
            }
        }

        return count;
    }

    public static void sellItem() {}
}
