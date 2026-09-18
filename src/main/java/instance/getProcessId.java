package instance;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.ptr.IntByReference;

public class getProcessId {

    public static int getMinecraftPID() {
        final int[] processID = {-1};

        User32.INSTANCE.EnumWindows((hwnd, data) -> {
            char[] buffer = new char[512];

            User32.INSTANCE.GetWindowText(
                hwnd,
                buffer,
                512
            );

            String title =
                Native.toString(buffer);

            if (!title.contains("Minecraft")) {
                return true;
            }

            IntByReference pid =
                new IntByReference();

            User32.INSTANCE.GetWindowThreadProcessId(
                hwnd,
                pid
            );

            int currentPID =
                pid.getValue();

            try {
                String commandLine =
                    getCommandLine.getCommandLine(
                        currentPID
                    );
                    
                if (commandLine.contains("--gameDir") || commandLine.contains("-Djava.library.path=") || commandLine.contains("com/mojang")) {
                    processID[0] =
                        currentPID;

                    return false;
                }

            } catch (Exception e) {
                return true;
            }

            return true;

        }, Pointer.NULL);

        return processID[0];
    }
}