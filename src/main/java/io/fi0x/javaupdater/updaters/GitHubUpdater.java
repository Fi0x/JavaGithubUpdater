package io.fi0x.javaupdater.updaters;

import io.fi0x.javaupdater.IUpdater;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InvalidObjectException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class GitHubUpdater implements IUpdater
{
    private final String releaseUrl; // = "https://api.github.com/repos/Fi0x/EDCT/releases";
    private final String currentVersion;
    private final String expectedAssetName;

    private String newestVersion;
    Map<String, ArrayList<String>> releases;

    public GitHubUpdater(String releaseUrl, String currentVersion, String expectedAssetName)
    {
        this.releaseUrl = releaseUrl;
        this.currentVersion = currentVersion;
        this.expectedAssetName = expectedAssetName;
    }

    @Override
    public boolean hasNewerVersion() throws IOException, InterruptedException
    {
        loadNewestVersion();
        return isNewer(currentVersion, newestVersion);
    }

    @Override
    public String getDownloadUrl() throws IOException, InterruptedException
    {
        loadNewestVersion();
        return isNewer(currentVersion, newestVersion) ? releases.get(newestVersion).get(1) : null;
    }

    @Override
    public String getWebsiteUrl() throws IOException, InterruptedException
    {
        loadNewestVersion();
        return isNewer(currentVersion, newestVersion) ? releases.get(newestVersion).get(0) : null;
    }

    private void loadNewestVersion() throws IOException, InterruptedException
    {
        if (releases == null)
        {
            String response = getReleases();
            if (response == null || response.isEmpty())
                throw new InvalidObjectException("Could not retrieve the correct release-json from the update-url");

            releases = getReleases(response);
        }

        if (newestVersion == null)
        {
            newestVersion = currentVersion;

            for (Map.Entry<String, ArrayList<String>> version : releases.entrySet())
            {
                if (isNewer(newestVersion, version.getKey())) newestVersion = version.getKey();
            }
        }
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

    public String getReleases() throws IOException, InterruptedException
    {
        return sendHTTPRequest(releaseUrl, "GET", new HashMap<>(), true);
    }

    public Map<String, ArrayList<String>> getReleases(String jsonString)
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
                String downloadUrl = getAssetUrl(assets);

                ArrayList<String> urls = new ArrayList<>();
                urls.add(url);
                urls.add(downloadUrl);

                releaseDates.put(tag, urls);
            }
        } catch (org.json.simple.parser.ParseException e)
        {
            System.out.println("Could not convert release-json: " + jsonString);
            e.printStackTrace();
        }

        return releaseDates;
    }

    private String getAssetUrl(JSONArray jsonAssets)
    {
        for (Object asset : jsonAssets)
        {
            JSONObject assetJson = (JSONObject) asset;

            if (expectedAssetName == null || expectedAssetName.equals(assetJson.get("name")))
                return assetJson.get("browser_download_url").toString();
        }

        return null;
    }

    public static String sendHTTPRequest(String endpoint, String requestType, Map<String, String> parameters, boolean ignore429)
            throws IOException, InterruptedException
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
            throw new ConnectException();
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
