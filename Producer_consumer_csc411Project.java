
package producer_consumer_csc411project;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.*;

/**
 *
 * @author MKHATHAZI
 */
public class Producer_consumer_csc411Project {

 
    
    public static Queue<Integer> fileBuffer = new LinkedList<Integer>();
    public static final int BUFFER_CAPACITY = 10; // Max elements in buffer, as per requirement
    public static volatile boolean producerFinished = false; // Flag for consumer to know producer is done
    public static final int TOTAL_FILES_TO_PRODUCE = 10; // Based on the original writeXML_file loop (fileCount <= 10)
    public static final String XML_FILE_PATH_PREFIX = "D:/to_file/student_"; // Base path for XML files

    
    public static void main(String[] args)  throws Exception {
        System.out.println("Starting Producer-Consumer simulation with buffer capacity: " + BUFFER_CAPACITY);

        // Reset state for multiple runs or clarity, though generally only run once per execution
        fileBuffer.clear();
        producerFinished = false;

        // Create producer and consumer tasks
        Runnable producerTask = new producerTask();
        Runnable consumerTask = new ConsumerTask();

        // Create threads for producer and consumer
        Thread producerThread = new Thread(producerTask, "ProducerThread");
        Thread consumerThread = new Thread(consumerTask, "ConsumerThread");

        // Start the threads
        producerThread.start();
        consumerThread.start();

        // Wait for both threads to complete
        producerThread.join();
        consumerThread.join();

        System.out.println("\nComplete");
        System.out.println("\nFinal Buffer Contents - should be empty");
        System.out.println("Integers in buffer: " + fileBuffer);
        System.out.println("\n");

        // Post-simulation menu
        displayMenu();
    }

  
    public static String getElementTextContent(Element parentElement, String tagName) {
        NodeList nodeList = parentElement.getElementsByTagName(tagName);
        if (nodeList.getLength() > 0) {
            return nodeList.item(0).getTextContent();
        }
        return ""; // Return an empty string if the element is not found
    }


    public static void readAndPrintXmlFile(Integer fileNum, boolean deleteAfterProcessing) throws Exception {
        if (fileNum == null) {
            System.err.println(Thread.currentThread().getName() + ": readAndPrintXmlFile: Received null file number, skipping.");
            return;
        }

        String filePath = XML_FILE_PATH_PREFIX + fileNum + ".xml";
        File xmlFile = new File(filePath);

        if (!xmlFile.exists()) {
            System.err.println(Thread.currentThread().getName() + ": Error: XML file not found at " + filePath + ". Skipping " + (deleteAfterProcessing ? "deletion" : "viewing") + " for this entry.");
            return;
        }

        // Only print "Consuming file" if it's during the actual consumer phase, not just viewing
        if (Thread.currentThread().getName().startsWith("ConsumerThread")) {
            System.out.println(Thread.currentThread().getName() + ": --- Consuming file: " + xmlFile.getName() + " ---");
        } else {
            System.out.println("\n****************************************************************\nContent of : " + xmlFile.getName() + " file\n");

        }
        


        // XML parsing setup
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(xmlFile); // Parse the XML file into a Document
        document.getDocumentElement().normalize(); // Normalize the document for consistent structure

        // Get the root element (expected to be "Programme")
        Element root = document.getDocumentElement();

        // Extract data for StudentData object using the helper method
        String studentId = getElementTextContent(root, "ID");
        String studentName = getElementTextContent(root, "student");
        String courseName = getElementTextContent(root, "course");

        // Parse numerical and decision data, handling potential parsing errors
        int caGrade = 0;
        int examGrade = 0;
        double finalMark = 0.0;
        String decision = "";

        try {
            caGrade = Integer.parseInt(getElementTextContent(root, "CA"));
            examGrade = Integer.parseInt(getElementTextContent(root, "EXAM"));
            finalMark = Double.parseDouble(getElementTextContent(root, "finalMark"));
            decision = getElementTextContent(root, "status");
        } catch (NumberFormatException e) {
            System.err.println(Thread.currentThread().getName() + ": Warning: Could not parse numeric data from " +
                    xmlFile.getName() + ": " + e.getMessage() + ". Defaulting to 0/0.0 for grades.");
            // Default values (0 or 0.0) will be used if parsing fails
        }

        // Create a StudentData object to hold the extracted information
        StudentData studentData = new StudentData(studentId, studentName, courseName,
                caGrade, examGrade, finalMark, decision);

        // Print the extracted student information to the console
        System.out.println(studentData);
        System.out.println("\n****************************************************************");

        // Delete the XML file after processing, if requested
        if (deleteAfterProcessing) {
            if (xmlFile.delete()) {
                System.out.println(Thread.currentThread().getName() + ": Successfully deleted XML file: " + filePath);
            } else {
                System.err.println(Thread.currentThread().getName() + ": Failed to delete XML file: " + filePath + ". Please check file permissions.");
            }
        }
    }


   
    private static void displayMenu() {
        Scanner input = new Scanner(System.in);
        int choice;

        do {
          
   System.out.println("\n\n*****************************************************\n*****************************************************");
   System.out.println("\tSELECT AN OPTION TO PERFORM (1 - 3)");
  
  System.out.println("\t1. View content of each XML file\n\t2. Delete XML files\n\t3. Exit program");
            System.out.print("\n\tEnter your choice :   ");

            while (!input.hasNextInt()) {
                System.out.println("Invalid input. Please enter a number(1 - 3).");
                input.next(); // consume the invalid input
                System.out.print("RE-SELECT AN OPTION TO PERFORM : ");
            }
            choice = input.nextInt();
            input.nextLine(); // consume newline character

            switch (choice) {
                case 1:
                    viewAllXmlFiles();
                    break;
                case 2:
                    deleteAllXmlFiles();
                    break;
                case 3:
                    System.out.println("Exiting program..... Goodbye!");
                    break;
                default:
                    System.out.println("\tInvalid choice. Please enter 1, 2, or 3.");
            }
        } while (choice != 3);

        input.close();
    }


    private static void viewAllXmlFiles() {
        System.out.println("\n\n\t --- Viewing all XML files, one by one \n\n");
        for (int i = 1; i <= TOTAL_FILES_TO_PRODUCE; i++) {
            try {
                // Call readAndPrintXmlFile but with deleteAfterProcessing = false
                readAndPrintXmlFile(i, false);
            } catch (Exception e) {
                System.err.println("Error viewing file student_" + i + ".xml: " + e.getMessage());
                
            }
        }
        System.out.println("\n\nDone viewing XML files form folder : "+XML_FILE_PATH_PREFIX+"\n\n");
    }

   
    private static void deleteAllXmlFiles() {
        System.out.println("\n Deleting XML files ............");
        boolean anyDeleted = false;
        for (int i = 1; i <= TOTAL_FILES_TO_PRODUCE; i++) {
            String filePath = XML_FILE_PATH_PREFIX + i + ".xml";
            File xmlFile = new File(filePath);
            if (xmlFile.exists()) {
                if (xmlFile.delete()) {
                    System.out.println("Successfully deleted: " + filePath); // deleting file one by one
                    anyDeleted = true;
                } else {
                    System.err.println("Failed to delete: " + filePath + ". Check file permissions.");
                }
            } else {

                 System.out.println("File not found, skipping deletion: " + filePath);
            }
        }
        if (!anyDeleted) {
            System.out.println("No XML files were found or deleted.");
        }
        System.out.println(".........Done deleting XML files *************");
            
        
        
    }
}