package io.fi0x.javaupdater.urls;

import io.fi0x.javaupdater.IUrlHolder;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FixedUrlHolder implements IUrlHolder
{
    private final String releaseUrl;

    @Override
    public String getReleaseUrl()
    {
        return releaseUrl;
    }
}
