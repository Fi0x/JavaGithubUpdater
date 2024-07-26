package io.fi0x.javaupdater.logic;

import java.util.ArrayList;

public class Comparator
{
    static boolean isNewer(String originalVersion, String newVersion)
    {
        ArrayList<Integer> currentParts = new ArrayList<>();
        ArrayList<Integer> nextParts = new ArrayList<>();

        //TODO: Also allow Strings in the names and catch parsing errors (make this overall more robust)

        for (String part : originalVersion.replace(".", "-").split("-"))
            currentParts.add(Integer.parseInt(part));

        for (String part : newVersion.replace(".", "-").split("-"))
            nextParts.add(Integer.parseInt(part));

        for (int i = 0; i < currentParts.size() && i < nextParts.size(); i++)
        {
            if (currentParts.get(i) < nextParts.get(i)) return true;
            else if (currentParts.get(i) > nextParts.get(i)) return false;
        }

        return currentParts.get(3) < nextParts.get(3);
    }
}
