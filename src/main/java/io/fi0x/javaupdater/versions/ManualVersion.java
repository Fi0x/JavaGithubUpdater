package io.fi0x.javaupdater.versions;

import io.fi0x.javaupdater.IVersion;
import lombok.AllArgsConstructor;
import lombok.Setter;

@Setter
@AllArgsConstructor
public class ManualVersion implements IVersion
{
    private String currentVersion;

    @Override
    public String getCurrentVersion()
    {
        return currentVersion;
    }
}
