package instance;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class getMinecraftVersion {

    public static String get(int pid) {
        String commandLine =
            getCommandLine.getCommandLine(pid);

        Pattern pattern =
            Pattern.compile(
                "com/mojang/minecraft/([^/]+)/minecraft-[^/]+\\.jar"
            );

        Matcher matcher =
            pattern.matcher(commandLine);

        if (matcher.find()) {
            return matcher.group(1);
        }

        return null;
    }
}