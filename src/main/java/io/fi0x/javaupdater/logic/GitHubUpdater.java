package io.fi0x.javaupdater.logic;

import io.fi0x.javadatastructures.Tuple;
import io.fi0x.javaupdater.IUpdater;
import io.fi0x.javaupdater.IUrlHolder;
import io.fi0x.javaupdater.IVersion;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Log
@RequiredArgsConstructor
public class GitHubUpdater implements IUpdater
{
    private final IVersion currentVersion;
    private final IUrlHolder releaseUrl;
    private final String expectedAssetName;
    private final boolean allowPreReleases;

    private String newestVersion;
    private Map<String, ArrayList<String>> releases;

    @Override
    public boolean hasNewerVersion() throws IOException, ParseException
    {
        loadNewestVersion();

        return Comparator.isNewer(currentVersion.getCurrentVersion(), newestVersion);
    }

    @Override
    public String getDownloadUrl() throws IOException, ParseException
    {
        loadNewestVersion();

        if (Comparator.isNewer(currentVersion.getCurrentVersion(), newestVersion))
            return releases.get(newestVersion).get(1);
        return null;
    }

    @Override
    public String getWebsiteUrl() throws IOException, ParseException
    {
        loadNewestVersion();

        if (Comparator.isNewer(currentVersion.getCurrentVersion(), newestVersion))
            return releases.get(newestVersion).get(0);
        return null;
    }

    private void loadNewestVersion() throws IOException, ParseException
    {
        if (newestVersion != null)
            return;

        if (releases == null)
        {
            String response = WebRequester.getHTTPRequest(releaseUrl.getReleaseUrl(), new HashMap<>());
            if (response.isEmpty())
                throw new InvalidObjectException("Could not retrieve the correct release-json from the update-url");

            loadReleases(response);
        }

        newestVersion = currentVersion.getCurrentVersion();

        for (Map.Entry<String, ArrayList<String>> version : releases.entrySet())
        {
            if (Comparator.isNewer(newestVersion, version.getKey()))
                newestVersion = version.getKey();
        }
    }

    private void loadReleases(String jsonString) throws ParseException
    {
        JSONArray jsonReleases = getJsonReleases(jsonString);

        releases = new HashMap<>();
        for (Object release : jsonReleases)
        {
            JSONObject releaseJson = (JSONObject) release;
            if (!allowPreReleases && (boolean) releaseJson.get("prerelease"))
                continue;
            if ((boolean) releaseJson.get("draft"))
                continue;

            Tuple<String, ArrayList<String>> entry = getReleaseEntry(releaseJson);
            releases.put(entry.getObject1(), entry.getObject2());
        }
    }

    private JSONArray getJsonReleases(String jsonString) throws ParseException
    {
        try
        {
            return (JSONArray) new JSONParser().parse(jsonString);
        } catch (ParseException e)
        {
            log.warning("Could not convert release-json: " + jsonString);
            throw e;
        }
    }

    private Tuple<String, ArrayList<String>> getReleaseEntry(JSONObject releaseJson)
    {
        String tag = releaseJson.get("tag_name").toString();
        String url = releaseJson.get("html_url").toString();

        JSONArray assets = (JSONArray) releaseJson.get("assets");
        String downloadUrl = getAssetUrl(assets);

        ArrayList<String> urls = new ArrayList<>();
        urls.add(url);
        urls.add(downloadUrl);

        return new Tuple<>(tag, urls);
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
}
