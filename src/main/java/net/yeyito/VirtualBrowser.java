package net.yeyito;

import com.beust.jcommander.internal.Nullable;
import net.yeyito.util.StringFilter;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.zip.DeflaterInputStream;
import java.util.zip.GZIPInputStream;

public class VirtualBrowser {
    static String user_agent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/109.0.0.0 Safari/537.36";
    public List<String> cookies = new ArrayList<>();
    public boolean printCURL = false;
    public boolean logCookiesCURL = true;
    public HashMap<String,Object> openWebsite(String site, String requestMethod,@Nullable HashMap<String,String> requestHeaders, @Nullable String[] requestCookies, @Nullable String payload, boolean logCookies, boolean print) throws IOException {
        // Opening Connection
        HttpURLConnection connection = (HttpURLConnection) new URL(site).openConnection();
        // Setting Request Method
        connection.setRequestMethod(requestMethod);
        // Adding Headers
        if (requestHeaders != null) {
            for (String headerName : requestHeaders.keySet()) {
                connection.addRequestProperty(headerName, requestHeaders.get(headerName));
                if (print) {
                    System.out.println("Header: " + headerName + ": " + requestHeaders.get(headerName));
                }
            }
        }
        // Adding Cookies
        if (requestCookies != null) {
            for (String cookie : requestCookies) {
                connection.addRequestProperty("cookie", cookie);
                if (print) {
                    System.out.println("Cookie: " + cookie);
                }
            }
        }
        // Adding Payload
        if (Objects.equals(requestMethod, "POST") && payload != null) {
            connection.setDoOutput(true);
            connection.setFixedLengthStreamingMode(payload.getBytes().length);
            OutputStream os = connection.getOutputStream();
            os.write(payload.getBytes());
            os.flush();
            os.close();
            if (print) {System.out.println("Payload added. conent-length set to: " + payload.getBytes().length);}
        } else if (Objects.equals(requestMethod,"POST")) {
            connection.setDoOutput(true);
            connection.setFixedLengthStreamingMode(0);
        }

        // * RESPONSE * //
        // Logging Response Cookies
        if (logCookies) {
            List<String> cookiesInRequest = connection.getHeaderFields().get("set-cookie");
            if (cookiesInRequest != null) {
                for (String cookie : cookiesInRequest) {
                    String cookieName = StringFilter.parseStringUsingRegex(cookie, "(.*?)=");
                    String cookieValue = StringFilter.parseStringUsingRegex(cookie, "=(.*?);");

                    this.cookies.removeIf(savedCookie -> savedCookie.contains(cookieName)); //Remove if contains cookie name since cookie = cookiename+=+value
                    this.cookies.add(cookieName + "=" + cookieValue);

                    if (print) {
                        System.out.println("Server returned cookie: " + cookieName + "=" + cookieValue);
                    }
                }
            }
        }
        // Getting Possible Errors
        if (connection.getErrorStream() != null) {
            InputStream response = connection.getErrorStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(response));
            String line;
            StringBuilder content = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }
            reader.close();
            response.close();
            System.out.println(content);
        }

        // Getting Response
        String serverResponse = null;
        if (connection.getInputStream().available() > 0) {
            InputStream response = connection.getInputStream();
            if ("gzip".equals(connection.getContentEncoding())) {
                response = new GZIPInputStream(response);
            }
            if ("deflate".equals(connection.getContentEncoding())) {
                response = new DeflaterInputStream(response);
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(response));
            String line;
            StringBuilder content = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }
            reader.close();
            response.close();

            serverResponse = content.toString();
            if (print) {
                System.out.println("Server returned response: " + serverResponse);
            }
        }
        // Getting Response Headers
        Map<String, List<String>> responseHeaders = connection.getHeaderFields();
        if (print) {
            for (String s : responseHeaders.keySet()) {
                System.out.println("Response header: " + s + ": " + connection.getHeaderFields().get(s));
            }
        }
        // Getting Response Code
        int responseCode = connection.getResponseCode();
        if (print) {System.out.println("Response code: " + responseCode);}

        HashMap<String, Object> returnObject = new HashMap<>();
        returnObject.put("responseCode",responseCode);
        returnObject.put("responseHeaders",responseHeaders);
        returnObject.put("response",serverResponse);

        return returnObject;
    }

    public HashMap<String, Object> curlToOpenWebsite(String cURL) throws IOException {
        String site = StringFilter.parseStringUsingRegex(cURL,"'(.*?)'");
        String requestMethod = "GET";
        HashMap<String, String> requestHeaders = new HashMap<>();
        String[] requestCookies = buildCookies(this.cookies,null);
        String payload = null;

        String[] arguments = cURL.split("\\\\");
        for (String argument: arguments) {
            if (argument.contains("-H '")) {requestHeaders.put(StringFilter.parseStringUsingRegex(argument,"-H '(.*?):").stripLeading().stripTrailing(),StringFilter.parseStringUsingRegex(argument,StringFilter.parseStringUsingRegex(argument,"-H '(.*?):") + ":(.*?)'").stripLeading().stripTrailing());}
            else if (argument.contains("--data-raw '")) {payload = StringFilter.parseStringUsingRegex(argument,"'(.*?)' \\\\"); requestMethod = "POST";}
        }

        return openWebsite(site, requestMethod, requestHeaders, requestCookies, payload, this.logCookiesCURL, this.printCURL);
    }

    public HashMap<String, Object> curlToOpenWebsite(String cURL,String header) throws IOException {
        String site = StringFilter.parseStringUsingRegex(cURL,"'(.*?)'");
        String requestMethod = "GET";
        HashMap<String, String> requestHeaders = new HashMap<>();
        String[] requestCookies = buildCookies(this.cookies,null);
        String payload = null;

        String[] arguments = cURL.split("\\\\");
        for (String argument: arguments) {
            if (argument.contains("-H '")) {requestHeaders.put(StringFilter.parseStringUsingRegex(argument,"-H '(.*?):").stripLeading().stripTrailing(),StringFilter.parseStringUsingRegex(argument,StringFilter.parseStringUsingRegex(argument,"-H '(.*?):") + ":(.*?)'").stripLeading().stripTrailing());}
            else if (argument.contains("--data-raw '")) {payload = StringFilter.parseStringUsingRegex(argument,"'(.*?)'"); requestMethod = "POST";}
        }
        requestHeaders.put(StringFilter.parseStringUsingRegex(header,"-H '(.*?):").stripLeading().stripTrailing(),StringFilter.parseStringUsingRegex(header,StringFilter.parseStringUsingRegex(header,"-H '(.*?):") + ":(.*?)'").stripLeading().stripTrailing());
        return openWebsite(site, requestMethod, requestHeaders, requestCookies, payload, this.logCookiesCURL, this.printCURL);
    }

    public HashMap<String, Object> curlToOpenWebsite(String cURL,String[] headers) {
        try {
            String site = StringFilter.parseStringUsingRegex(cURL,"'(.*?)'");
            String requestMethod = "GET";
            HashMap<String, String> requestHeaders = new HashMap<>();
            String[] requestCookies = buildCookies(this.cookies,null);
            String payload = null;

            String[] arguments = cURL.split("\\\\");
            for (String argument: arguments) {
                if (argument.contains("-H '")) {requestHeaders.put(StringFilter.parseStringUsingRegex(argument,"-H '(.*?):").stripLeading().stripTrailing(),StringFilter.parseStringUsingRegex(argument,StringFilter.parseStringUsingRegex(argument,"-H '(.*?):") + ":(.*?)'").stripLeading().stripTrailing());}
                else if (argument.contains("--data-raw '")) {payload = StringFilter.parseStringUsingRegex(argument,"'(.*?)' \\\\"); requestMethod = "POST";}
            }

            for (String header: headers) {
                requestHeaders.put(StringFilter.parseStringUsingRegex(header,"-H '(.*?):").stripLeading().stripTrailing(),StringFilter.parseStringUsingRegex(header,StringFilter.parseStringUsingRegex(header,"-H '(.*?):") + ":(.*?)'").stripLeading().stripTrailing());
            }

            return openWebsite(site, requestMethod, requestHeaders, requestCookies, payload, this.logCookiesCURL, this.printCURL);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public String[] buildCookies(List<String> preset,@Nullable String[] remove) {
        String[] newCookies;

        if (remove != null) {
            for (String cookie : remove) {
                preset.remove(cookie);
            }
        }

        newCookies = new String[preset.size()];

        for (int i = 0; i < preset.size(); i++) {
            newCookies[i] = preset.get(i);
        }

        return newCookies;
    }

    public HashMap<String,String> buildHeaders(@Nullable String preset, @Nullable HashMap<String,String> add, @Nullable String[] remove) {
        HashMap<String,String> headers = new HashMap<>();
        if (preset != null) {headers.putAll(getHeadersPreset(preset));}
        if (remove != null) {
            for (String key: remove) {
                if (headers.containsKey(key)) {
                    headers.remove(key);
                } else {
                    System.out.println("Bad remove key! " + key + " headers doesn't contain it.");
                }
            }
        }
        if (add != null) {headers.putAll(add);}
        return headers;
    }

    @Deprecated public HashMap<String,String> getHeadersPreset(String preset) {
        HashMap<String,String> headers = new HashMap<>();
        if (Objects.equals(preset, "roblox-login")) {
            headers.put("accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
            headers.put("accept-encoding","gzip, deflate, br");
            headers.put("accept-language","en-US,en;q=0.9");
            headers.put("sec-ch-ua","\"Not_A Brand\";v=\"99\", \"Google Chrome\";v=\"109\", \"Chromium\";v=\"109\"");
            headers.put("sec-ch-ua-mobile","?0");
            headers.put("sec-ch-ua-platform","\"Windows\"");
            headers.put("sec-fetch-dest","document");
            headers.put("sec-fetch-mode","navigate");
            headers.put("sec-fetch-site","none");
            headers.put("sec-fetch-user","?1");
            headers.put("upgrade-insecure-requests","1");
            headers.put("user-agent",user_agent);
        }
        if (Objects.equals(preset,"roblox-POST")) {
            headers.put("accept","*/*");
            headers.put("accept-encoding","gzip, deflate, br");
            headers.put("accept-language","en-US,en;q=0.9");
            headers.put("origin","https://www.roblox.com");
            headers.put("referer","https://www.roblox.com/login"); // watch out for this one
            headers.put("sec-ch-ua","\"Not_A Brand\";v=\"99\", \"Google Chrome\";v=\"109\", \"Chromium\";v=\"109\"");
            headers.put("sec-ch-ua-mobile","?0");
            headers.put("sec-ch-ua-platform","\"Windows\"");
            headers.put("sec-fetch-dest","empty");
            headers.put("sec-fetch-mode","cors");
            headers.put("sec-fetch-site","same-origin");
            headers.put("user-agent",user_agent);
        }
        return headers;
    }
}
