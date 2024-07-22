package io.fi0x.javaupdater;

public interface IUpdater
{
    boolean hasNewerVersion();
    String getDownloadUrl();
    String getWebsiteUrl();
}
