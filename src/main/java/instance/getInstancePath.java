package instance;

import com.sun.jna.Library;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.platform.win32.BaseTSD;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.platform.win32.WinNT.MEMORY_BASIC_INFORMATION;
import com.sun.jna.ptr.IntByReference;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class getInstancePath {

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
        Pointer address,
        String label
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
                label + " ReadProcessMemory failed: "
                + Kernel32.INSTANCE.GetLastError()
                + " | address=" + address
                + " | bytesRead=" + bytesRead.getValue()
            );
        }

        long value = Native.POINTER_SIZE == 8
            ? buffer.getLong(0)
            : buffer.getInt(0) & 0xFFFFFFFFL;

        return new Pointer(value);
    }

    private static Map<String, String> getEnvironment(
        HANDLE process
    ) {
        PROCESS_BASIC_INFORMATION pbi =
            new PROCESS_BASIC_INFORMATION();

        IntByReference retLen =
            new IntByReference();

        int status = Ntdll.INSTANCE.NtQueryInformationProcess(
            process,
            0,
            pbi,
            pbi.size(),
            retLen
        );

        if (status != 0) {
            throw new IllegalStateException(
                "NtQueryInformationProcess failed: " + status
            );
        }

        Pointer processParameters =
            readPointer(
                process,
                pbi.PebBaseAddress.share(
                    Native.POINTER_SIZE == 8 ? 0x20 : 0x10
                ),
                "getEnvironment ProcessParameters"
            );

        Pointer environmentAddress =
            readPointer(
                process,
                processParameters.share(
                    Native.POINTER_SIZE == 8 ? 0x80 : 0x48
                ),
                "getEnvironment Environment"
            );

        if (environmentAddress == null) {
            return new HashMap<>();
        }

        MEMORY_BASIC_INFORMATION memoryInfo =
            new MEMORY_BASIC_INFORMATION();

        BaseTSD.SIZE_T result =
            Kernel32.INSTANCE.VirtualQueryEx(
                process,
                environmentAddress,
                memoryInfo,
                new BaseTSD.SIZE_T(memoryInfo.size())
            );

        if (result.longValue() == 0) {
            throw new IllegalStateException(
                "VirtualQueryEx failed: "
                + Kernel32.INSTANCE.GetLastError()
            );
        }

        long regionEnd =
            Pointer.nativeValue(memoryInfo.baseAddress)
            + memoryInfo.regionSize.longValue();

        long readableBytes =
            regionEnd
            - Pointer.nativeValue(environmentAddress);

        if (readableBytes <= 0) {
            throw new IllegalStateException(
                "Invalid environment memory region."
            );
        }

        Memory buffer = new Memory(readableBytes);
        IntByReference bytesRead = new IntByReference();

        boolean ok = Kernel32.INSTANCE.ReadProcessMemory(
            process,
            environmentAddress,
            buffer,
            (int) readableBytes,
            bytesRead
        );

        if (!ok || bytesRead.getValue() <= 0) {
            throw new IllegalStateException(
                "ReadProcessMemory failed: "
                + Kernel32.INSTANCE.GetLastError()
                + " | bytesRead=" + bytesRead.getValue()
            );
        }

        String envBlock = new String(
            buffer.getByteArray(0, bytesRead.getValue()),
            StandardCharsets.UTF_16LE
        );

        int end = envBlock.indexOf("\u0000\u0000");

        if (end != -1) {
            envBlock = envBlock.substring(0, end);
        }

        Map<String, String> environment = new HashMap<>();

        for (String var : envBlock.split("\u0000")) {
            int index = var.indexOf('=');

            if (index > 0) {
                environment.put(
                    var.substring(0, index),
                    var.substring(index + 1)
                );
            }
        }

        return environment;
    }

    private static Path checkInstMcDir(
        Map<String, String> environment
    ) {
        String value = environment.get("INST_MC_DIR");

        if (value != null && !value.isEmpty()) {
            Path path = Paths.get(value);

            if (Files.isDirectory(path)) {
                return path;
            }
        }

        return null;
    }

    private static Path checkGameDir(String commandLine) {
        String[] args = commandLine.split("\\s+");

        for (int i = 0; i < args.length - 1; i++) {
            if (args[i].equals("--gameDir")) {
                Path path = Paths.get(args[i + 1]);

                if (Files.isDirectory(path)) {
                    return path;
                }
            }
        }

        return null;
    }

    private static Path checkJavaLibraryPath(
        String commandLine
    ) {
        String prefix = "-Djava.library.path=";

        for (String arg : commandLine.split("\\s+")) {
            if (!arg.startsWith(prefix)) {
                continue;
            }

            String[] libraryPaths =
                arg.substring(prefix.length()).split(
                    java.util.regex.Pattern.quote(
                        java.io.File.pathSeparator
                    )
                );

            for (String libraryPathString : libraryPaths) {
                Path libraryPath =
                    Paths.get(libraryPathString);

                Path minecraft =
                    libraryPath.resolveSibling(".minecraft");

                if (Files.isDirectory(minecraft)) {
                    return minecraft;
                }

                minecraft =
                    libraryPath.resolveSibling("minecraft");

                if (Files.isDirectory(minecraft)) {
                    return minecraft;
                }
            }
        }

        return null;
    }

    private static String getCommandLine(HANDLE process) {
        PROCESS_BASIC_INFORMATION pbi =
            new PROCESS_BASIC_INFORMATION();

        IntByReference retLen =
            new IntByReference();

        int status = Ntdll.INSTANCE.NtQueryInformationProcess(
            process,
            0,
            pbi,
            pbi.size(),
            retLen
        );

        if (status != 0) {
            throw new IllegalStateException(
                "NtQueryInformationProcess failed: " + status
            );
        }

        Pointer processParameters =
            readPointer(
                process,
                pbi.PebBaseAddress.share(
                    Native.POINTER_SIZE == 8 ? 0x20 : 0x10
                ),
                "getCommandLine ProcessParameters"
            );

        Memory unicodeString = new Memory(16);
        IntByReference bytesRead =
            new IntByReference();

        boolean ok = Kernel32.INSTANCE.ReadProcessMemory(
            process,
            processParameters.share(
                Native.POINTER_SIZE == 8 ? 0x70 : 0x40
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
                Native.POINTER_SIZE == 8 ? 8 : 4
            );

        Memory commandLineMemory =
            new Memory(length);

        ok = Kernel32.INSTANCE.ReadProcessMemory(
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
            commandLineMemory.getByteArray(0, length),
            StandardCharsets.UTF_16LE
        );
    }

    public static Path getInstancePath(int pid) {
        HANDLE process =
            Kernel32.INSTANCE.OpenProcess(
                0x0410,
                false,
                pid
            );

        if (process == null) {
            return null;
        }

        try {
            Map<String, String> environment =
                getEnvironment(process);

            String commandLine =
                getCommandLine(process);

            Path instancePath =
                checkInstMcDir(environment);

            if (instancePath == null) {
                instancePath =
                    checkGameDir(commandLine);
            }

            if (instancePath == null) {
                instancePath =
                    checkJavaLibraryPath(commandLine);
            }

            return instancePath;
        } finally {
            Kernel32.INSTANCE.CloseHandle(process);
        }
    }
}