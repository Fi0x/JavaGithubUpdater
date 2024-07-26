package io.fi0x.javaupdater.logic;

import lombok.extern.java.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Log
class WebRequester
{
    private static final int TIMEOUT = 5000;

    static String getHTTPRequest(String endpoint, Map<String, String> parameters) throws IOException
    {
        endpoint += getParamsString(parameters);
        URL url = cleanUpUrl(endpoint);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/109.0.0.0 Safari/537.36");

        con.setConnectTimeout(TIMEOUT);
        con.setReadTimeout(TIMEOUT);

        int status;
        try
        {
            status = con.getResponseCode();
        } catch (IOException e)
        {
            log.warning("Could not establish a connection to the server for request: " + endpoint);
            throw new ConnectException(e.getMessage());
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
                log.warning("A http request timed out");
                throw new ConnectException(e.getMessage());
            }
            in.close();
        } else if (status != 0)
            log.warning("Received a bad HTTP response: " + status + "\n\tFor url: " + url);

        con.disconnect();

        return content.toString();
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
        return !resultString.isEmpty() ? "?" + resultString.substring(0, resultString.length() - 1) : "";
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
