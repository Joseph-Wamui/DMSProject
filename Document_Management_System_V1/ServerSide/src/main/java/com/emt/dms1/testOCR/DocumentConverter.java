package com.emt.dms1.testOCR;

import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

public class DocumentConverter {

    public static void main(String[] args) {
        // Create a file chooser
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Choose a Document File");

        // Show the file chooser dialog
        int result = fileChooser.showOpenDialog(null);

        // Check if the user selected a file
        if (result == JFileChooser.APPROVE_OPTION) {
            // Get the selected file
            File selectedFile = fileChooser.getSelectedFile();

            // Perform document conversion
            convertToPlainText(selectedFile);
        } else {
            System.out.println("No file selected. Exiting program.");
        }
    }

    private static void convertToPlainText(File file) {
        try (FileInputStream inputStream = new FileInputStream(file);
             Scanner scanner = new Scanner(inputStream, "UTF-8")) {

            // Read the content of the document
            StringBuilder content = new StringBuilder();
            while (scanner.hasNextLine()) {
                content.append(scanner.nextLine()).append("\n");
            }

            // Display the converted text
            System.out.println("Converted Document Text:\n" + content.toString());

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.out.println("File not found: " + file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error reading the file: " + file.getAbsolutePath());
        }
    }
}