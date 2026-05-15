package faststt.manager;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import java.util.Properties;
import java.util.List;
import fastaudio.FastAudioCapture;
import faststt.core.PathResolver;

/**
 * FastSTT Installer - Handles local installation and model management for Whisper.
 */
public class FastSTTInstaller {

    private final Scanner scanner = new Scanner(System.in);

    public void run() {
        while (true) {
            clearConsole();
            System.out.println("========================================");
            System.out.println("   FastSTT Installer — Whisper Native");
            System.out.println("========================================\n");

            System.out.println("  1.  [Model]  List/Download Whisper Models (GGML)");
            System.out.println("  2.  [Native] Download Whisper DLL & Headers (Windows)");
            System.out.println("  3.  [Audio]  Test Input Device (FastAudioCapture)");
            System.out.println("  4.  [Config] Setup Cloud Keys (Deepgram/OpenAI)");
            System.out.println("  5.  [Path]   Toggle Local/Global Install (" + PathResolver.getInstallDir() + ")");
            System.out.println("  q.  Quit");

            System.out.print("\nChoose an option: ");
            String choice = scanner.nextLine();

            switch (choice.toLowerCase()) {
                case "1": manageWhisperModels(); break;
                case "2": downloadNativeBinaries(); break;
                case "3": testAudio(); break;
                case "4": configureCloud(); break;
                case "5": manageInstallPath(); break;
                case "q": return;
            }
        }
    }

    private void manageWhisperModels() {
        while (true) {
            clearConsole();
            System.out.println("--- Whisper GGML Models ---");
            File modelDir = new File("models");
            if (!modelDir.exists()) modelDir.mkdir();

            File[] models = modelDir.listFiles((d, n) -> n.endsWith(".bin"));
            if (models != null && models.length > 0) {
                System.out.println("Installed Models:");
                for (File m : models) System.out.println("  ✓ " + m.getName());
            } else {
                System.out.println("No models installed.");
            }

            System.out.println("\nAvailable to Download:");
            System.out.println("  [t] tiny   (~75 MB)   - Fast, lower accuracy");
            System.out.println("  [b] base   (~150 MB)  - Recommended for start");
            System.out.println("  [s] small  (~500 MB)  - High accuracy");
            System.out.println("  [back] Back");

            System.out.print("\nCommand: ");
            String cmd = scanner.nextLine().toLowerCase();

            if (cmd.equals("back")) return;
            
            String modelUrl = "";
            String fileName = "";
            
            if (cmd.equals("t")) {
                fileName = "ggml-tiny.bin";
                modelUrl = "https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-tiny.bin";
            } else if (cmd.equals("b")) {
                fileName = "ggml-base.bin";
                modelUrl = "https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-base.bin";
            } else if (cmd.equals("s")) {
                fileName = "ggml-small.bin";
                modelUrl = "https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-small.bin";
            }

            if (!modelUrl.isEmpty()) {
                downloadFile(modelUrl, fileName, "models");
            }
        }
    }

    private void downloadNativeBinaries() {
        clearConsole();
        System.out.println("--- Whisper Native Binaries (Windows x64) ---");
        System.out.println("This will download 'whisper.dll' and 'whisper.h' for development.");
        
        System.out.print("\nDo you want to download now? (y/n): ");
        if (!scanner.nextLine().equalsIgnoreCase("y")) return;

        File nativeDir = new File("native/include");
        if (!nativeDir.exists()) nativeDir.mkdirs();
        File buildDir = new File("build");
        if (!buildDir.exists()) buildDir.mkdirs();

        String baseUrl = "https://github.com/ggerganov/whisper.cpp/releases/download/v1.7.1/";
        downloadFile(baseUrl + "whisper-cpp-v1.7.1-bin-win-msvc-x64.zip", "whisper_temp.zip", ".");
        
        System.out.println("\n[INFO] Extracting binaries...");
        try {
            String psCommand = 
                "Expand-Archive -Path 'whisper_temp.zip' -DestinationPath 'whisper_extract' -Force; " +
                "Copy-Item -Path 'whisper_extract/bin/*.dll' -Destination 'build/' -Force; " +
                "Copy-Item -Path 'whisper_extract/bin/*.dll' -Destination 'native/' -Force; " +
                "if (Test-Path 'whisper_extract/include') { Copy-Item -Path 'whisper_extract/include/*' -Destination 'native/include/' -Recurse -Force }; " +
                "Remove-Item -Path 'whisper_extract' -Recurse -Force; " +
                "Remove-Item -Path 'whisper_temp.zip' -Force";
            new ProcessBuilder("powershell", "-Command", psCommand).inheritIO().start().waitFor();
            System.out.println("[SUCCESS] Native binaries installed in build/ and native/include/");
        } catch (Exception e) {
            System.err.println("[ERROR] Extraction failed: " + e.getMessage());
        }
        
        System.out.println("\n[Press Enter]");
        scanner.nextLine();
    }

    private void downloadFile(String urlString, String fileName, String targetDir) {
        System.out.println("\n[INFO] Downloading " + fileName + "...");
        File outputFile = new File(targetDir, fileName);
        
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            long fileSize = connection.getContentLengthLong();
            
            try (BufferedInputStream in = new BufferedInputStream(connection.getInputStream());
                 FileOutputStream out = new FileOutputStream(outputFile)) {
                
                byte[] dataBuffer = new byte[8192];
                int bytesRead;
                long totalBytesRead = 0;
                
                while ((bytesRead = in.read(dataBuffer, 0, 8192)) != -1) {
                    totalBytesRead += bytesRead;
                    out.write(dataBuffer, 0, bytesRead);
                    
                    if (fileSize > 0) {
                        drawProgressBar(totalBytesRead, fileSize);
                    }
                }
            }
            System.out.println("\n[SUCCESS] " + fileName + " installed.");
        } catch (Exception e) {
            System.err.println("\n[ERROR] Download failed: " + e.getMessage());
            if (outputFile.exists()) outputFile.delete();
        }
    }

    private void drawProgressBar(long current, long total) {
        int percent = (int) (current * 100 / total);
        int barLength = 30;
        int completed = (int) (current * barLength / total);
        
        StringBuilder bar = new StringBuilder("[");
        for (int i = 0; i < barLength; i++) {
            if (i < completed) bar.append("█");
            else bar.append("░");
        }
        bar.append("] ").append(percent).append("% (")
           .append(current / 1024 / 1024).append("/")
           .append(total / 1024 / 1024).append(" MB)\r");
        
        System.out.print(bar.toString());
    }

    private void testAudio() {
        clearConsole();
        System.out.println("--- Audio Input Test (via FastAudioCapture) ---");
        
        try {
            System.out.println("Available Devices:");
            String[] devices = FastAudioCapture.getCaptureDevices();
            for (int i = 0; i < devices.length; i++) {
                System.out.println("  " + (i + 1) + ". " + devices[i]);
            }

            System.out.println("\nStarting default device for level test (3 seconds)...");
            FastAudioCapture capture = new FastAudioCapture();
            if (capture.startRecording(16000, 1, 16)) { // Whisper prefers 16kHz mono
                for (int i = 0; i < 30; i++) {
                    float level = capture.getLevel();
                    int bars = (int) (level * 40);
                    String meter = "█".repeat(Math.max(0, bars)) + "░".repeat(Math.max(0, 40 - bars));
                    System.out.print("\r  Level: [" + meter + "] " + (int)(level * 100) + "%  ");
                    Thread.sleep(100);
                }
                capture.stopRecording();
                capture.close();
                System.out.println("\n\n[SUCCESS] Audio capture verified.");
            } else {
                System.err.println("[ERROR] Could not start recording.");
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Audio test failed: " + e.getMessage());
        }
        
        System.out.println("\n[Press Enter to return]");
        scanner.nextLine();
    }

    private void manageInstallPath() {
        clearConsole();
        System.out.println("--- Installation Path Management ---");
        System.out.println("Current effective path: " + PathResolver.getInstallDir());
        System.out.println("\nOptions:");
        System.out.println("  1. Use Local (./models, ./native)");
        System.out.println("  2. Use Global (C:\\ProgramData\\FastSTT)");
        System.out.println("  b. Back");

        System.out.print("\nChoose: ");
        String choice = scanner.nextLine();
        
        if (choice.equals("2")) {
            File globalDir = new File("C:\\ProgramData\\FastSTT");
            if (!globalDir.exists()) {
                System.out.println("[INFO] Creating global directory...");
                globalDir.mkdirs();
            }
            saveConfig("install.dir", globalDir.getAbsolutePath());
            System.out.println("[SUCCESS] Global path set. Please move your models manually if needed.");
        } else if (choice.equals("1")) {
            saveConfig("install.dir", ".");
            System.out.println("[SUCCESS] Local path set.");
        }
        
        System.out.println("\n[Press Enter]");
        scanner.nextLine();
    }

    private void saveConfig(String key, String value) {
        Properties props = new Properties();
        File configFile = new File("faststt.properties");
        if (configFile.exists()) {
            try (FileInputStream fis = new FileInputStream(configFile)) {
                props.load(fis);
            } catch (Exception ignored) {}
        }
        props.setProperty(key, value);
        try (FileOutputStream fos = new FileOutputStream(configFile)) {
            props.store(fos, "FastSTT Configuration");
        } catch (Exception e) {
            System.err.println("[ERROR] Could not save config: " + e.getMessage());
        }
    }

    private void clearConsole() {
        try {
            new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
        } catch (Exception ignored) {}
    }

    public static void main(String[] args) {
        new FastSTTInstaller().run();
    }
}
