package io.fi0x.javaupdater.urls;

import io.fi0x.javaupdater.IUrlHolder;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class GithubUrlHolder implements IUrlHolder
{
    private final String GITHUB_URL = "https://api.github.com/repos/";
    private final String RELEASE = "/releases";
    private String ownerName;
    private String projectName;

    @Override
    public String getReleaseUrl()
    {
        return GITHUB_URL + ownerName + "/" + projectName + RELEASE;
    }
}
