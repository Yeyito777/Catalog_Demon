package net.yeyito;

import com.beust.jcommander.internal.Nullable;
import net.yeyito.connections.ProxyUtil;
import net.yeyito.roblox.LimitedPriceTracker;
import net.yeyito.util.JSON;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.zip.GZIPInputStream;

public class WebsiteScraper {
    WebDriver currentDriver;
    boolean builtCookies = false;
    static HashMap<String,String> cookies = new HashMap<>();
    static final int millisToWaitIf429 = 1000;
    static final String[] userAgents = new String[]{
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.82 Safari/537.36",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.82 Safari/537.36",
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.82 Safari/537.36",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:79.0) Gecko/20100101 Firefox/79.0",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:79.0) Gecko/20100101 Firefox/79.0",
            "Mozilla/5.0 (X11; Linux i686; rv:79.0) Gecko/20100101 Firefox/79.0",
            "Mozilla/5.0 (iPhone; CPU iPhone OS 14_4_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/14.0 Mobile/15E148 Safari/604.1",
            "Mozilla/5.0 (iPad; CPU OS 14_4_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/14.0 Mobile/15E148 Safari/604.1",
            "Mozilla/5.0 (Windows NT 10.0; Trident/7.0; rv:11.0) like Gecko",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.82 Safari/537.36"
    };

    public WebsiteScraper(boolean headless) {
        System.setProperty("webdriver.chrome.driver", "C:\\Users\\aline\\OneDrive\\Desktop\\CodeHelpers\\ChromeDriver\\chromedriver.exe");

        if (headless) {
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless");
            currentDriver = new ChromeDriver(options);
        } else {
            currentDriver = new ChromeDriver();
        }
    }

    public void openURL(String URL) throws IOException {
        this.currentDriver.get(URL);
        WebDriverWait wait = new WebDriverWait(this.currentDriver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.jsReturnsValue("return document.readyState==\"complete\";"));
    }

    public void scrapeOpenedURL() {
        String html = this.currentDriver.getPageSource();
        System.out.println(Jsoup.parse(html));
    }

    public void getCatalogIDsFromOpenedURL() {
        String html = this.currentDriver.getPageSource();
        List<String> IDs = StringFilter.parseStringUsingRegexMatchAllDigits(html, "catalog/(.*?)/");
        TextFile textFile = new TextFile("Limiteds.txt");

        for (String ID: IDs) {
            if (!textFile.findString(ID)) {
                textFile.writeString(ID + "\n");
                System.out.println("Wrote ID: " + ID);
            }
        }
    }
    public void buildCookies(String URL) {
        this.currentDriver.get(URL);
        Cookie robloSecurityCookie = new Cookie(".ROBLOSECURITY", Main.key_of_the_day, ".roblox.com", "/", null, false);
        this.currentDriver.manage().addCookie(robloSecurityCookie);

        this.builtCookies = true;
    }
    public Long itemToPriceSelenium(long ID) {
        String URL = "https://economy.roblox.com/v1/assets/" + ID + "/resellers?cursor=&limit=100";
        if (!this.builtCookies) {this.buildCookies(URL);}

        this.currentDriver.get(URL);
        String html = this.currentDriver.getPageSource();

        return StringFilter.extractLowestPriceFromHTML(html);
    }

    public static HashMap<Long, Long> itemBulkToPrice(List<Long> IDs) throws IOException {
        buildCookiesForBulkRequest();

        if (IDs.size() <= 120) {
            return itemBulkToPriceRequest(IDs);
        } else {
            int requestNumber = (IDs.size() + 119) / 120;
            List<List<Long>> listOfIDsLists = new ArrayList<>();

            for (int i = 0; i < requestNumber; i++) {
                int fromIndex = i * 120;
                int toIndex = Math.min(fromIndex + 120, IDs.size());
                List<Long> listOfIDs = IDs.subList(fromIndex, toIndex);
                listOfIDsLists.add(listOfIDs);
            }

            HashMap<Long,Long> IDsToPriceHashMap = new HashMap<>();
            for (List<Long> IDs120: listOfIDsLists) {
                IDsToPriceHashMap.putAll(itemBulkToPriceRequest(IDs120));
            }
            return IDsToPriceHashMap;
        }
    }

    public static void requestCookiesFromURL(String site,@Nullable String[] requestCookies, String requestType, int requestProperties, @Nullable String payload, boolean print) throws IOException {
        URL url = new URL(site);
        HttpURLConnection authConnection = (HttpURLConnection) url.openConnection();
        addDefaultPropertiesToRequest(authConnection,requestProperties);
        authConnection.setRequestMethod(requestType);
        addCookiesToRequest(authConnection,requestCookies);

        if (Objects.equals(requestType,"POST") && payload != null) {
            authConnection.setDoOutput(true);
            authConnection.setFixedLengthStreamingMode(payload.getBytes().length);
            OutputStream os = authConnection.getOutputStream();
            os.write(payload.getBytes());
            os.flush();
            os.close();
        }

        List<String> cookiesInRequest = authConnection.getHeaderFields().get("set-cookie");
        if (cookiesInRequest != null) {
            for (String cookie : cookiesInRequest) {
                String cookieName = StringFilter.parseStringUsingRegex(cookie, "(.*?)=");
                String cookieValue = StringFilter.parseStringUsingRegex(cookie, "=(.*?);");

                cookies.remove(cookieName);
                cookies.put(cookieName, cookieName + "=" + cookieValue);
            }
        }

        if (Objects.equals(requestType, "GET")) {
            InputStream response = authConnection.getInputStream();
            if("gzip".equals(authConnection.getContentEncoding())){
                response = new GZIPInputStream(response);
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(response));
            String line;
            StringBuilder content = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }
            reader.close();
            response.close();

            if (print) {
                System.out.println("Response body: " + content.toString());
            }

            String token = StringFilter.parseStringUsingRegex(content.toString(),"<meta name=\"csrf-token\" data-token=\"(.*?)\"");
            if (token != null && !token.isEmpty()) {
                cookies.remove("x-csrf-token");
                cookies.put("x-csrf-token",token);
            }
        }

        if (print) {
            System.out.println(authConnection.getResponseCode());
            System.out.println("Response: " + authConnection.getResponseMessage() + "\n");

            for (String s: authConnection.getHeaderFields().keySet()) {
                System.out.println(s + " = " + authConnection.getHeaderFields().get(s));
            }

            System.out.println(cookies);
        }
    }

    public static void buildCookiesForBulkRequest() throws IOException {
        requestCookiesFromURL("https://www.roblox.com/catalog?Category=1&salesTypeFilter=2",null,"GET",0,null,false);
    }

    public static HashMap<Long,Long> itemBulkToPriceRequest(List<Long> IDs) throws IOException {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL("https://catalog.roblox.com/v1/catalog/items/details").openConnection();
            connection.setRequestMethod("POST");
            addDefaultPropertiesToRequest(connection, 1);
            addCookiesToRequest(connection,
                    new String[]{
                            cookies.get("rbx-ip-2"),
                            cookies.get("RBXEventTrackerV2"),
                            cookies.get("GuestData"),
                    });
            connection.addRequestProperty("x-csrf-token", cookies.get("x-csrf-token"));

            connection.setDoOutput(true);

            StringBuilder sb = new StringBuilder();
            sb.append("{\"items\":[");
            for (int i = 0; i < IDs.size(); i++) {
                sb.append("{\"id\":").append(IDs.get(i)).append(",\"itemType\":\"Asset\",\"key\":\"Asset_").append(IDs.get(i)).append("\",\"thumbnailType\":\"Asset\"}");
                if (i != IDs.size() - 1) {
                    sb.append(",");
                }
            }
            sb.append("]}");

            OutputStream os = connection.getOutputStream();
            os.write(sb.toString().getBytes());
            os.flush();
            os.close();

            InputStream response = connection.getInputStream();
            if ("gzip".equals(connection.getContentEncoding())) {
                response = new GZIPInputStream(response);
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(response));
            String line;
            StringBuilder content = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }
            reader.close();
            response.close();

            System.out.println("Request Done!");
            return JSON.itemBatchStringToHashMap(content.toString());
        } catch (IOException e) {
            System.out.println("Error: " + e);

            // VPN activate

            buildCookiesForBulkRequest(); // Regenerate Cookies
            return itemBulkToPriceRequest(IDs); // Try Again with new cookies
        }
    }

    public static void addCookiesToRequest(HttpURLConnection connection, String[] requestCookies) {
        if (requestCookies != null) {
            for (String cookie : requestCookies) {
                connection.addRequestProperty("cookie", cookie);
            }
        }
    }
    public static void addDefaultPropertiesToRequest(HttpURLConnection connection, int reqType) {
        if (reqType == 0) {
            connection.addRequestProperty("accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
            connection.addRequestProperty("accept-encoding","gzip, deflate, br");
            connection.addRequestProperty("accept-language","es-PA,es;q=0.9");
            connection.addRequestProperty("sec-ch-ua","Not_A Brand");
            connection.addRequestProperty("sec-ch-ua","v=\"99\", \"Google Chrome\"");
            connection.addRequestProperty("sec-ch-ua","v=\"109\", \"Chromium\";v=\"109\"");
            connection.addRequestProperty("sec-ch-ua-mobile","?0");
            connection.addRequestProperty("sec-ch-ua-platform","\"Windows\"");
            connection.addRequestProperty("sec-fetch-dest","document");
            connection.addRequestProperty("sec-fetch-mode","navigate");
            connection.addRequestProperty("sec-fetch-site","none");
            connection.addRequestProperty("sec-fetch-user","?1");
            connection.addRequestProperty("upgrade-insecure-requests","1");
            connection.addRequestProperty("user-agent",userAgents[new Random().nextInt(0,userAgents.length)]);
        }
        else if (reqType == 1) {
            connection.addRequestProperty("accept","application/json, text/plain, */*");
            connection.addRequestProperty("accept-encoding","gzip, deflate, br");
            connection.addRequestProperty("accept-language","es-PA,es;q=0.9");
            connection.addRequestProperty("content-type","application/json;charset=UTF-8");
            connection.addRequestProperty("origin","https://www.roblox.com");
            connection.addRequestProperty("referer","https://www.roblox.com/");
            connection.addRequestProperty("sec-ch-ua","Not_A Brand");
            connection.addRequestProperty("sec-ch-ua","v=\"99\", \"Google Chrome\"");
            connection.addRequestProperty("sec-ch-ua","v=\"109\", \"Chromium\";v=\"109\"");
            connection.addRequestProperty("sec-ch-ua-mobile","?0");
            connection.addRequestProperty("sec-ch-ua-platform","\"Windows\"");
            connection.addRequestProperty("sec-fetch-dest","empty");
            connection.addRequestProperty("sec-fetch-mode","cors");
            connection.addRequestProperty("sec-fetch-site","same-site");
            connection.addRequestProperty("user-agent",userAgents[new Random().nextInt(0,userAgents.length)]);

        }
    }

    public static Long itemToPrice(long ID) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL("https://economy.roblox.com/v1/assets/" + ID + "/resellers?cursor=&limit=100")
                .openConnection();

        connection.setRequestMethod("GET");
        connection.addRequestProperty("cookie", ".ROBLOSECURITY=" + Main.key_of_the_day);

        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder result = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            result.append(line);
        }
        reader.close();
        return StringFilter.extractLowestPriceFromHTML(result.toString());
    }
    public static Long itemToRAP(long ID) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL("https://economy.roblox.com/v1/assets/" + ID + "/resale-data")
                .openConnection();

        connection.setRequestMethod("GET");

        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder result = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            result.append(line);
        }
        reader.close();
        return StringFilter.extractRAPFromHTML(result.toString());
    }

    @Deprecated public static String scrapeRobloxCatalogID(long ID) throws IOException {
        return scrapeURL("https://www.roblox.com/catalog/" + ID);
    }

    @Deprecated public static String scrapeURL(String URL) throws IOException {
        // Open a connection to the website
        HttpURLConnection con = (HttpURLConnection) new URL(URL).openConnection();

        // Set the request method to GET
        con.setRequestMethod("GET");

        // Read the response from the website
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();

            // Print the HTML content of the website
            return content.toString();
        } catch (Exception e) {return "";}
    }

    @Deprecated public static void scrapeRobloxIDAsync(long ID) {
        String URL = "https://www.roblox.com/catalog/" + ID;
        CompletableFuture<Document> completableFuture = CompletableFuture.supplyAsync(() -> {
            try {
                return Jsoup.connect(URL).get();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        });

        completableFuture.thenAccept(document -> {
            LimitedPriceTracker.LimitedToPrice.put(ID,Long.parseLong(StringFilter.extractRobuxFromHTML(document.toString())));
        });
    }
}
