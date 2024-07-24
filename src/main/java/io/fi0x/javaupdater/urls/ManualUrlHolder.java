package io.fi0x.javaupdater.urls;

import io.fi0x.javaupdater.IUrlHolder;
import lombok.AllArgsConstructor;
import lombok.Setter;

@Setter
@AllArgsConstructor
public class ManualUrlHolder implements IUrlHolder
{
    private String releaseUrl;

    @Override
    public String getReleaseUrl()
    {
        return releaseUrl;
    }
}
