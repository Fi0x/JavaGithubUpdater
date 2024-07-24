package io.fi0x.javaupdater;

import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.InvalidObjectException;

public interface IUpdater
{
    boolean hasNewerVersion() throws IOException, ParseException;
    String getDownloadUrl() throws IOException, ParseException;
    String getWebsiteUrl() throws IOException, ParseException;
}
