package net.yeyito.roblox;

import net.yeyito.Main;
import net.yeyito.VirtualBrowser;
import net.yeyito.connections.DiscordBot;
import net.yeyito.util.StringFilter;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.awt.event.InputEvent;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ItemManager {
    public static void buyItem(long id) throws IOException, InterruptedException, URISyntaxException {
        Main.discordBot.sendMessageOnRegisteredChannels("Buying item: " + id,0);
        String expected_price = "";
        String seller_id = "";
        String expected_currency = "";
        String productID = "";
        String token = "";
        String user_asset_id = "";

        VirtualBrowser virtualBrowser = new VirtualBrowser();
        String getItem = (String) virtualBrowser.curlToOpenWebsite("curl 'https://www.roblox.com/catalog/"+id+"' \\\n" +
                "  -H 'cookie: "+Main.secCookie+"' \\\n").get("response");

        String[] searches = {"data-product-id=", "data-token=", "data-expected-price=", "data-expected-seller-id=", "data-expected-currency=", "data-lowest-private-sale-userasset-id="};

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
                    }
                }
            }
        }

        System.out.println("Product ID: " + productID);
        System.out.println("Token: " + token);
        System.out.println("Expected Price: " + expected_price);
        System.out.println("Seller ID: " + seller_id);
        System.out.println("Expected Currency: " + expected_currency);
        System.out.println("User Assed ID: " + user_asset_id);

        if (Integer.parseInt(expected_price) < 21) {
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
            System.err.println("Sudden price change of item: " + id + " detected price: " + expected_price);
        }
    }
    public static void sellItem() {}
}
