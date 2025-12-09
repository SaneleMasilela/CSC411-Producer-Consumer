
package producer_consumer_csc411project;


public class StudentData {
    String studentId;
    String studentName;
    String programme = "Programme"; // Assumed from root element name "Programme"
    String courseName;
    int caGrade;
    int examGrade;
    double finalMark;
    String decision;

    /**
     * Constructor for StudentData.
     */
    public StudentData(String studentId, String studentName, String courseName,
                       int caGrade, int examGrade, double finalMark, String decision) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.courseName = courseName;
        this.caGrade = caGrade;
        this.examGrade = examGrade;
        this.finalMark = finalMark;
        this.decision = decision;
    }



    public String toString() {
        return "Student ID : " + studentId +
               "\nStudent Name : " + studentName +
               "\nCourse : " + courseName +
               "\nCA Grade : " + caGrade + "%" +
               "\nExam Grade : " + examGrade + "%" +
               "\nAverage Mark : " + String.format("%.2f", finalMark) + "%" +
               "\nDecision : " + decision;
    }
}
