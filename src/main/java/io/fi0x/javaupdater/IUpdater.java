package io.fi0x.javaupdater;

import java.io.IOException;
import java.io.InvalidObjectException;

public interface IUpdater
{
    boolean hasNewerVersion() throws IOException, InterruptedException;
    String getDownloadUrl() throws IOException, InterruptedException;
    String getWebsiteUrl() throws IOException, InterruptedException;
}
