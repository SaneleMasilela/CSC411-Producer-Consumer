
package producer_consumer_csc411project;
import java.io.File.*;

public class ConsumerTask implements Runnable {

   
    public void run() {
        System.out.println(Thread.currentThread().getName() + ": Starting XML file consumption...");
        while (true) {
            Integer fileNum = null;
            synchronized (Producer_consumer_csc411Project.fileBuffer) {
                // Wait if buffer is empty AND producer is not finished
                while (Producer_consumer_csc411Project.fileBuffer.isEmpty() && !Producer_consumer_csc411Project.producerFinished) {
                    try {
                        System.out.println(Thread.currentThread().getName() + ": Buffer is empty. Waiting for producer...");
                        Producer_consumer_csc411Project.fileBuffer.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        System.err.println(Thread.currentThread().getName() + " interrupted while waiting.");
                        return; // Exit if interrupted
                    }
                }

                // If producer is finished and buffer is empty, then consumer is done
                if (Producer_consumer_csc411Project.fileBuffer.isEmpty() && Producer_consumer_csc411Project.producerFinished) {
                    System.out.println(Thread.currentThread().getName() + ": Producer finished and buffer is empty. Exiting.");
                    break; // Exit the loop
                }

                // Consume an item
                fileNum = Producer_consumer_csc411Project.fileBuffer.poll();
                if (fileNum != null) {
                    System.out.println(Thread.currentThread().getName() + ": Polled file number " + fileNum + ". Buffer size: " + Producer_consumer_csc411Project.fileBuffer.size());
                    Producer_consumer_csc411Project.fileBuffer.notifyAll(); // Notify producer that space is available
                }
            }

            if (fileNum != null) {
                try {
                    // Process this specific file (read and print, but DO NOT delete here)
                    Producer_consumer_csc411Project.readAndPrintXmlFile(fileNum, false); // false = do not delete
                } catch (Exception e) {
                    System.err.println(Thread.currentThread().getName() + " caught an exception while processing file " + fileNum + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
        System.out.println(Thread.currentThread().getName() + ": XML file consumption complete.");
    }
}