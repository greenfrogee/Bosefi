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
            User32.INSTANCE.GetWindowText(hwnd, buffer, 512);

            String title = Native.toString(buffer);

            if (title.contains("Minecraft")) {
                IntByReference pid =
                    new IntByReference();

                User32.INSTANCE.GetWindowThreadProcessId(
                    hwnd,
                    pid
                );

                processID[0] = pid.getValue();

                return false;
            }

            return true;
        }, Pointer.NULL);

        return processID[0];
    }
}