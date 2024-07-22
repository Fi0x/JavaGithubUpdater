package io.fi0x.javaupdater;

import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class JavaUpdater
{
    private static String version;
    private static final String releaseUrl = "https://api.github.com/repos/Fi0x/EDCT/releases";

    public static void checkForUpdates()
    {
    }

    public static ArrayList<String> getNewerVersion()
    {
        String response = getReleases();
        if (response == null || response.equals("")) return null;

        Map<String, ArrayList<String>> releases = getReleases(response);

        String newestVersion = version;
        for (Map.Entry<String, ArrayList<String>> version : releases.entrySet())
        {
            if (isNewer(newestVersion, version.getKey())) newestVersion = version.getKey();
        }

        return isNewer(version, newestVersion) ? releases.get(newestVersion) : null;
    }

    private static boolean isNewer(String currentVersion, String nextVersion)
    {
        ArrayList<Integer> currentParts = new ArrayList<>();
        ArrayList<Integer> nextParts = new ArrayList<>();

        for (String part : currentVersion.replace(".", "-").split("-"))
        {
            currentParts.add(Integer.parseInt(part));
        }
        for (String part : nextVersion.replace(".", "-").split("-"))
        {
            nextParts.add(Integer.parseInt(part));
        }

        if (currentParts.get(0) < nextParts.get(0)) return true;
        else if (currentParts.get(0) > nextParts.get(0)) return false;

        if (currentParts.get(1) < nextParts.get(1)) return true;
        else if (currentParts.get(1) > nextParts.get(1)) return false;

        if (currentParts.get(2) < nextParts.get(2)) return true;
        else if (currentParts.get(2) > nextParts.get(2)) return false;

        return currentParts.get(3) < nextParts.get(3);
    }

    public static String getReleases()
    {
        Map<String, String> params = new HashMap<>();

        int counter = 0;
        while (counter < 3)
        {
            counter++;
            try
            {
                return sendHTTPRequest(releaseUrl, "GET", params, true);
            } catch (InterruptedException ignored)
            {
                return null;
            } catch (IOException e)
            {
                System.out.println("Warning: Could not find out if there is a newer version");
                e.printStackTrace();
                return null;
            } catch (HtmlConnectionException ignored)
            {
            }
        }
        return null;
    }

    public static Map<String, ArrayList<String>> getReleases(String jsonString)
    {
        Map<String, ArrayList<String>> releaseDates = new HashMap<>();

        try
        {
            JSONArray jsonReleases = (JSONArray) new JSONParser().parse(jsonString);
            for (Object release : jsonReleases)
            {
                JSONObject releaseJson = (JSONObject) release;
                if ((boolean) releaseJson.get("prerelease")) continue;
                if ((boolean) releaseJson.get("draft")) continue;

                String tag = releaseJson.get("tag_name").toString();
                String url = releaseJson.get("html_url").toString();

                JSONArray assets = (JSONArray) releaseJson.get("assets");
                String runExeUrl = getAssetUrl(assets, Main.VersionType.PORTABLE);
                String installUrl = getAssetUrl(assets, Main.VersionType.INSTALLER);
                String jarUrl = getAssetUrl(assets, Main.VersionType.JAR);

                ArrayList<String> urls = new ArrayList<>();
                urls.add(url);
                urls.add(runExeUrl);
                urls.add(installUrl);
                urls.add(jarUrl);

                releaseDates.put(tag, urls);
            }
        } catch (org.json.simple.parser.ParseException e)
        {
            System.out.println("Could not convert release-json: " + jsonString);
            e.printStackTrace();
        }

        return releaseDates;
    }

    private static String getAssetUrl(JSONArray jsonAssets, Main.VersionType portable)
    {
        for (Object asset : jsonAssets)
        {
            JSONObject assetJson = (JSONObject) asset;
            switch (portable)
            {
                case PORTABLE:
                    if (assetJson.get("name").equals("EDCT.exe"))
                        return assetJson.get("browser_download_url").toString();
                    break;
                case INSTALLER:
                    if (assetJson.get("name").equals("edctsetup.exe"))
                        return assetJson.get("browser_download_url").toString();
                    break;
                case JAR:
                    if (assetJson.get("name").equals("EDCT.jar"))
                        return assetJson.get("browser_download_url").toString();
                    break;
            }
        }

        return null;
    }

    public static String sendHTTPRequest(String endpoint, String requestType, Map<String, String> parameters, boolean ignore429)
            throws IOException, InterruptedException, HtmlConnectionException
    {
        if (!canRequest(ignore429))
            return null;

        endpoint += getParamsString(parameters);
        URL url = cleanUpUrl(endpoint);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod(requestType);
        con.setRequestProperty("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/109.0.0.0 Safari/537.36");

        con.setConnectTimeout(5000);
        con.setReadTimeout(5000);

        int status;
        try
        {
            status = con.getResponseCode();
        } catch (IOException e)
        {
            System.out.println("Could not establish a connection to the server for request: " + endpoint);
            e.printStackTrace(); // Code 995
            throw new HtmlConnectionException();
        }
        StringBuilder content = new StringBuilder();
        if (status == 200)
        {
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            try
            {
                while ((inputLine = in.readLine()) != null)
                {
                    content.append(inputLine);
                }
            } catch (SocketTimeoutException e)
            {
                System.out.println("A http request timed out");
                e.printStackTrace();
            }
            in.close();
        } else if (status == 429)
            System.out.println("Received a 429 status code from a website\n\tUrl was: " + url); // Code 429
        else if (status != 0)
            System.out.println("Received a bad HTTP response: " + status + "\n\tFor url: " + url);

        con.disconnect();

        return content.toString();
    }

    private static boolean canRequest(boolean ignore429) throws IOException, InterruptedException
    {
        return true;
    }

    private static String getParamsString(Map<String, String> params)
    {
        StringBuilder result = new StringBuilder();

        for (Map.Entry<String, String> entry : params.entrySet())
        {
            result.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
            result.append("&");
        }

        String resultString = result.toString();
        return resultString.length() > 0 ? "?" + resultString.substring(0, resultString.length() - 1) : "";
    }

    private static URL cleanUpUrl(String endpoint) throws MalformedURLException
    {
        return new URL(endpoint
                .replace(" ", "%20")
                .replace("'", "%27")
                .replace("`", "%60")
        );
    }
}
