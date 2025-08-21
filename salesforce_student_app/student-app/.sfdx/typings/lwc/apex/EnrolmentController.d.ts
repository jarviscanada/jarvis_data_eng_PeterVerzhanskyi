declare module "@salesforce/apex/EnrolmentController.searchStudents" {
  export default function searchStudents(param: {keyword: any}): Promise<any>;
}
declare module "@salesforce/apex/EnrolmentController.searchCourses" {
  export default function searchCourses(param: {keyword: any}): Promise<any>;
}
declare module "@salesforce/apex/EnrolmentController.createEnrolment" {
  export default function createEnrolment(param: {studentId: any, courseId: any, status: any, grade: any, enrolmentDate: any, campus: any}): Promise<any>;
}
