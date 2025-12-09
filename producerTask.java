
package producer_consumer_csc411project;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;


public class producerTask implements Runnable {


    public void run() {
        System.out.println(Thread.currentThread().getName() + ": Starting XML file generation and buffer population...");
        try {
            generateXmlFilesAndPopulateBuffer();
            System.out.println(Thread.currentThread().getName() + ": XML file generation complete.");
        } catch (Exception e) {
            System.err.println(Thread.currentThread().getName() + ": Caught an exception: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Signal to consumers that production is finished
            synchronized (Producer_consumer_csc411Project.fileBuffer) {
                Producer_consumer_csc411Project.producerFinished = true; // Set the static flag in the main class
                Producer_consumer_csc411Project.fileBuffer.notifyAll(); // Wake up any waiting consumers so they can exit if buffer is empty
                System.out.println(Thread.currentThread().getName() + ": Signaled completion to consumers.");
            }
        }
    }

  
    private void generateXmlFilesAndPopulateBuffer() throws Exception {
        Vector<String> namesVector = new Vector<String>();
        // Placeholder path for names.txt - ensure this file exists or adapt path
        File nameFile = new File(Producer_consumer_csc411Project.XML_FILE_PATH_PREFIX.replace("student_", "") + "names.txt");
        Scanner input = null;

        try {
            input = new Scanner(nameFile);
            while (input.hasNextLine()) {
                String storeNames = input.nextLine();
                namesVector.add(storeNames);
            }
        } catch (FileNotFoundException e) {
            System.err.println("Error: names.txt file not found at " + nameFile.getAbsolutePath());
            System.err.println("Please ensure 'names.txt' exists in the specified directory or update the path in Assignment_csc411.java.");
            throw e; // Re-throw to propagate the error
        } finally {
            if (input != null) {
                input.close();
            }
        }

        // XML Document Building Setup
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();

        int fileCount = 1;
        while (fileCount <= Producer_consumer_csc411Project.TOTAL_FILES_TO_PRODUCE) {
            synchronized (Producer_consumer_csc411Project.fileBuffer) {
                // Producer waits if buffer is full
                while (Producer_consumer_csc411Project.fileBuffer.size() == Producer_consumer_csc411Project.BUFFER_CAPACITY) {
                    try {
                        System.out.println(Thread.currentThread().getName() + ": Buffer is full (" +
                                Producer_consumer_csc411Project.fileBuffer.size() + "). Waiting for consumer...");
                        Producer_consumer_csc411Project.fileBuffer.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        System.err.println(Thread.currentThread().getName() + " interrupted while waiting.");
                        return; // Exit if interrupted
                    }
                }

                // XML file generation logic
                Document document = builder.newDocument();
                Element root = document.createElement("Programme");
                document.appendChild(root);

                int IDnum = (int) (Math.random() * 100000000);
                int EXAM = (int) (Math.random() * 100);
                int CA = (int) (Math.random() * 100);

                // Local variables for current file's data
                String ID = "" + IDnum;
                if (ID.length() < 7) {
                    ID = ID + "66";
                } else if (ID.length() < 8) {
                    ID = ID + "2";
                }

                Element studentID_element = document.createElement("ID");
                studentID_element.appendChild(document.createTextNode(ID));
                root.appendChild(studentID_element);

                Element studentName_element = document.createElement("student");
                if (namesVector.size() >= fileCount) {
                    studentName_element.appendChild(document.createTextNode(namesVector.elementAt(fileCount - 1)));
                } else {
                    studentName_element.appendChild(document.createTextNode("Unknown Student " + fileCount));
                }
                root.appendChild(studentName_element);

                Element courseName_element = document.createElement("course");
                courseName_element.appendChild(document.createTextNode("CSC 411"));
                root.appendChild(courseName_element);

                Element CA_grade_element = document.createElement("CA");
                CA_grade_element.appendChild(document.createTextNode("" + CA));
                root.appendChild(CA_grade_element);

                Element exam_element = document.createElement("EXAM");
                exam_element.appendChild(document.createTextNode("" + EXAM));
                root.appendChild(exam_element);

                double finalGrade = (((double) EXAM * 0.7) + ((double) CA * 0.3));
                Element finalMark_element = document.createElement("finalMark");
                finalMark_element.appendChild(document.createTextNode(String.format("%.2f", finalGrade)));
                root.appendChild(finalMark_element);

                String decision_status;
                if (finalGrade >= 50) {
                    decision_status = "PASS";
                } else {
                    decision_status = "Fail";
                }
                Element status_element = document.createElement("status");
                status_element.appendChild(document.createTextNode(decision_status));
                root.appendChild(status_element);

                // Write XML to file
                DOMSource source = new DOMSource(document);
                String outputPath = Producer_consumer_csc411Project.XML_FILE_PATH_PREFIX + fileCount + ".xml";
                StreamResult result = new StreamResult(outputPath);
                transformer.transform(source, result);
                System.out.println(Thread.currentThread().getName() + ": Generated: " + outputPath);

                // Add file number to the buffer and notify consumers
                Producer_consumer_csc411Project.fileBuffer.add(fileCount);
                System.out.println(Thread.currentThread().getName() + ": Added " + fileCount + " to buffer. Current size: " + Producer_consumer_csc411Project.fileBuffer.size());
                Producer_consumer_csc411Project.fileBuffer.notifyAll(); // Notify consumer that space is available
            }
            fileCount++; // Increment for the next student to writes on a separate file
        }
    }
}
