package io.fi0x.javaupdater.versions;

import io.fi0x.javaupdater.IVersion;

public class MavenVersion implements IVersion
{
    private final Class<?> targetClass;
    private final Package targetPackage;

    public MavenVersion()
    {
        this.targetClass = null;
        this.targetPackage = null;
    }
    public MavenVersion(Class<?> targetClass)
    {
        this.targetClass = targetClass;
        this.targetPackage = null;
    }
    public MavenVersion(Package targetPackage)
    {
        this.targetClass = null;
        this.targetPackage = targetPackage;
    }

    @Override
    public String getCurrentVersion()
    {
        if(targetClass != null)
            return targetClass.getPackage().getImplementationVersion();

        if(targetPackage != null)
            return targetPackage.getImplementationVersion();

        return getClass().getPackage().getImplementationVersion();
    }
}
