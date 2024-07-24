package io.fi0x.javaupdater.versions;

import io.fi0x.javaupdater.IVersion;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FixedVersion implements IVersion
{
    private final String currentVersion;

    @Override
    public String getCurrentVersion()
    {
        return currentVersion;
    }
}
