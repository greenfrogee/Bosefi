package instance;

import com.sun.jna.Library;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.ptr.IntByReference;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class getCommandLine {

    public interface Ntdll extends Library {
        Ntdll INSTANCE = Native.load("Ntdll", Ntdll.class);

        int NtQueryInformationProcess(
            HANDLE processHandle,
            int processInformationClass,
            PROCESS_BASIC_INFORMATION processInformation,
            int processInformationLength,
            IntByReference returnLength
        );
    }

    public static class PROCESS_BASIC_INFORMATION extends Structure {
        public Pointer Reserved1;
        public Pointer PebBaseAddress;
        public Pointer Reserved2_0;
        public Pointer Reserved2_1;
        public Pointer UniqueProcessId;
        public Pointer Reserved3;

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList(
                "Reserved1",
                "PebBaseAddress",
                "Reserved2_0",
                "Reserved2_1",
                "UniqueProcessId",
                "Reserved3"
            );
        }
    }

    private static Pointer readPointer(
        HANDLE process,
        Pointer address
    ) {
        Memory buffer = new Memory(Native.POINTER_SIZE);
        IntByReference bytesRead = new IntByReference();

        boolean ok = Kernel32.INSTANCE.ReadProcessMemory(
            process,
            address,
            buffer,
            (int) buffer.size(),
            bytesRead
        );

        if (!ok || bytesRead.getValue() != buffer.size()) {
            throw new IllegalStateException(
                "ReadProcessMemory failed: "
                + Kernel32.INSTANCE.GetLastError()
            );
        }

        long value =
            Native.POINTER_SIZE == 8
                ? buffer.getLong(0)
                : buffer.getInt(0) & 0xFFFFFFFFL;

        return new Pointer(value);
    }

    public static String getCommandLine(int pid) {

        HANDLE process =
            Kernel32.INSTANCE.OpenProcess(
                0x0410,
                false,
                pid
            );

        if (process == null) {
            throw new IllegalStateException(
                "Failed to open process."
            );
        }

        try {
            PROCESS_BASIC_INFORMATION pbi =
                new PROCESS_BASIC_INFORMATION();

            IntByReference retLen =
                new IntByReference();

            int status =
                Ntdll.INSTANCE.NtQueryInformationProcess(
                    process,
                    0,
                    pbi,
                    pbi.size(),
                    retLen
                );

            if (status != 0) {
                throw new IllegalStateException(
                    "NtQueryInformationProcess failed: "
                    + status
                );
            }

            Pointer processParameters =
                readPointer(
                    process,
                    pbi.PebBaseAddress.share(
                        Native.POINTER_SIZE == 8
                            ? 0x20
                            : 0x10
                    )
                );

            Memory unicodeString =
                new Memory(16);

            IntByReference bytesRead =
                new IntByReference();

            boolean ok =
                Kernel32.INSTANCE.ReadProcessMemory(
                    process,
                    processParameters.share(
                        Native.POINTER_SIZE == 8
                            ? 0x70
                            : 0x40
                    ),
                    unicodeString,
                    16,
                    bytesRead
                );

            if (!ok) {
                throw new IllegalStateException(
                    "Failed to read command line structure."
                );
            }

            int length =
                Short.toUnsignedInt(
                    unicodeString.getShort(0)
                );

            Pointer buffer =
                unicodeString.getPointer(
                    Native.POINTER_SIZE == 8
                        ? 8
                        : 4
                );

            Memory commandLineMemory =
                new Memory(length);

            ok =
                Kernel32.INSTANCE.ReadProcessMemory(
                    process,
                    buffer,
                    commandLineMemory,
                    length,
                    bytesRead
                );

            if (!ok) {
                throw new IllegalStateException(
                    "Failed to read command line."
                );
            }

            return new String(
                commandLineMemory.getByteArray(
                    0,
                    length
                ),
                StandardCharsets.UTF_16LE
            );

        } finally {
            Kernel32.INSTANCE.CloseHandle(process);
        }
    }
}