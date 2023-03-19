package net.yeyito.util;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

public class TextFile {
    File textFile;

    public TextFile(String name) {
        this.textFile = new File(name);
        try {
            if (!textFile.exists()) {
                textFile.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeString(String data) {
        try {
            Path path = Paths.get(this.textFile.getPath());
            Files.write(path, data.getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean findString(String input) {
        try {
            Scanner scanner = new Scanner(this.textFile);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.contains(input)) {
                    scanner.close();
                    return true;
                }
            }
            scanner.close();
            return false;
        } catch (FileNotFoundException e) {e.printStackTrace(); return false;}
    }
    public void shuffleLines() throws IOException {
        File file = new File(this.textFile.getPath());
        ArrayList<String> lines = new ArrayList<>();

        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        while ((line = reader.readLine()) != null) {
            lines.add(line);
        }
        reader.close();

        Collections.shuffle(lines, new Random());

        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        for (String shuffledLine : lines) {
            writer.write(shuffledLine);
            writer.newLine();
        }
        writer.close();
    }

    public void deleteText(String target, boolean deleteAll) {
        try {
            File tempFile = new File(this.textFile.getPath() + ".tmp");
            try (BufferedReader reader = new BufferedReader(new FileReader(this.textFile));
                 BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
                String line;
                boolean isFirstOccurrence = true;
                while ((line = reader.readLine()) != null) {
                    if (isFirstOccurrence || deleteAll) {
                        String modifiedLine = line.replace(target, "");
                        if (!line.equals(modifiedLine)) {
                            isFirstOccurrence = false;
                        }
                        writer.write(modifiedLine);
                    } else {
                        writer.write(line);
                    }
                    writer.newLine();
                }
            }
            Files.delete(this.textFile.toPath());
            tempFile.renameTo(this.textFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deleteAllText() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(this.textFile))) {
            writer.write("");
        } catch (IOException e) {e.printStackTrace();}
    }

}
