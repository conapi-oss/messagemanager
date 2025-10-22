package at.conapi.messagemanager.bootstrap;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ScriptLauncher {

    public static void main(String[] args) {
        Path workingDir = getWorkingDirectory();
        String scriptPath = "./launch.sh";

        List<String> command = new ArrayList<>();
        command.add("/bin/bash");
        command.add(scriptPath);


        System.out.println("Launching from working directory: " + workingDir);
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.directory(workingDir.toFile());
        processBuilder.inheritIO();

        try {
            Process process = processBuilder.start();
            // For macOS, bring the launched application to the foreground
            if (System.getProperty("os.name").toLowerCase().contains("mac")) {
                Runtime.getRuntime().exec(new String[]{"osascript", "-e", "tell application \"System Events\" to set frontmost of every process whose unix id is " + process.pid() + " to true"});
            }
            // Exit the launcher process
            System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static Path getWorkingDirectory() {
        if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            Path appSupportDir = Path.of(System.getProperty("user.home"))
                    .resolve("Library")
                    .resolve("Application Support")
                    .resolve("conapi")
                    .resolve("Message Manager");

            // If directory exists and contains launch.sh, use it
            if (Files.exists(appSupportDir.resolve("bin").resolve("launch.sh"))) {
                return appSupportDir.resolve("bin");
            }
        }

        // Default: return current directory (bin)
        return Path.of("").toAbsolutePath();
    }
}