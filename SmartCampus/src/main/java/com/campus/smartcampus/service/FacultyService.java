package com.campus.smartcampus.service;

import com.campus.smartcampus.dto.AttendanceUpdateRequest;
import com.campus.smartcampus.dto.MarksUpdateRequest;
import com.campus.smartcampus.dto.StudentAttendanceMarksDTO;
import com.campus.smartcampus.exception.ForbiddenException;
import com.campus.smartcampus.exception.NotFoundException;
import com.campus.smartcampus.model.CourseOffering;
import com.campus.smartcampus.model.Exam;
import com.campus.smartcampus.model.Faculty;
import com.campus.smartcampus.model.Marks;
import com.campus.smartcampus.repository.AdminRepository;
import com.campus.smartcampus.repository.FacultyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FacultyService {

    private final FacultyRepository facultyRepository;
    private final AdminRepository adminRepository;

    public FacultyService(FacultyRepository facultyRepository, AdminRepository adminRepository) {
        this.facultyRepository = facultyRepository;
        this.adminRepository = adminRepository;
    }

    public Faculty getProfile(Long facultyId) {
        return facultyRepository.findById(facultyId)
                .orElseThrow(() -> new NotFoundException("Faculty not found with ID: " + facultyId));
    }

    public List<CourseOffering> getOfferings(Long facultyId) {
        validateFacultyExists(facultyId);
        return facultyRepository.getOfferings(facultyId);
    }

    public List<StudentAttendanceMarksDTO> getEnrolledStudents(Long facultyId, Long offeringId) {
        validateFacultyExists(facultyId);
        validateOfferingAssignment(facultyId, offeringId);
        return facultyRepository.getEnrolledStudentsWithPerformance(offeringId);
    }

    @Transactional
    public void updateClassesHeld(Long facultyId, Long offeringId, Integer classesHeld) {
        validateFacultyExists(facultyId);
        validateOfferingAssignment(facultyId, offeringId);

        if (classesHeld == null || classesHeld < 0) {
            throw new IllegalArgumentException("Classes held count cannot be negative.");
        }
        if (classesHeld > 999) {
            throw new IllegalArgumentException("Classes held cannot exceed 999.");
        }
        int maxAttended = facultyRepository.getMaxClassesAttended(offeringId);
        if (classesHeld < maxAttended) {
            throw new IllegalArgumentException("Classes held (" + classesHeld + ") cannot be lower than the "
                    + maxAttended + " classes already recorded as attended by a student.");
        }

        facultyRepository.updateClassesHeld(offeringId, classesHeld);
    }

    @Transactional
    public void updateAttendance(Long facultyId, AttendanceUpdateRequest req) {
        validateFacultyExists(facultyId);
        validateOfferingAssignment(facultyId, req.getOfferingId());
        validateStudentEnrolled(req.getStudentId(), req.getOfferingId());

        if (req.getClassesAttended() == null || req.getClassesAttended() < 0) {
            throw new IllegalArgumentException("Classes attended cannot be negative.");
        }

        Integer classesHeld = facultyRepository.getClassesHeld(req.getOfferingId());
        if (req.getClassesAttended() > classesHeld) {
            throw new IllegalArgumentException("Classes attended (" + req.getClassesAttended()
                    + ") cannot exceed total classes held (" + classesHeld + ") for this offering.");
        }

        facultyRepository.updateAttendance(req.getStudentId(), req.getOfferingId(), req.getClassesAttended());
    }

    @Transactional
    public void updateMarks(Long facultyId, MarksUpdateRequest req) {
        validateFacultyExists(facultyId);
        validateOfferingAssignment(facultyId, req.getOfferingId());
        validateStudentEnrolled(req.getStudentId(), req.getOfferingId());

        // Component ceilings 30 / 30 / 50 (raw total out of 110); also enforced by CK_MARKS_* in Oracle.
        checkRange("Continuous Assessment", req.getContinuousAssessmentMarks(), Marks.MAX_CA);
        checkRange("Midterm", req.getMidtermMarks(), Marks.MAX_MIDTERM);
        checkRange("Final exam", req.getFinalExamMarks(), Marks.MAX_FINAL);

        facultyRepository.updateMarks(req.getStudentId(), req.getOfferingId(),
                req.getContinuousAssessmentMarks(), req.getMidtermMarks(), req.getFinalExamMarks());
    }

    public List<Exam> getExams(Long facultyId) {
        validateFacultyExists(facultyId);
        return facultyRepository.getExamsForFaculty(facultyId);
    }

    private static void checkRange(String label, Double value, double max) {
        if (value == null || value < 0 || value > max) {
            throw new IllegalArgumentException(label + " marks must be between 0 and " + (int) max + ".");
        }
    }

    private void validateFacultyExists(Long facultyId) {
        if (!adminRepository.facultyExists(facultyId)) {
            throw new NotFoundException("Faculty member does not exist with ID: " + facultyId);
        }
    }

    private void validateOfferingAssignment(Long facultyId, Long offeringId) {
        if (!adminRepository.offeringExists(offeringId)) {
            throw new NotFoundException("Course offering " + offeringId + " does not exist.");
        }
        if (!facultyRepository.isAssignedToOffering(facultyId, offeringId)) {
            throw new ForbiddenException("Course offering " + offeringId + " is not assigned to faculty " + facultyId + ".");
        }
    }

    private void validateStudentEnrolled(Long studentId, Long offeringId) {
        if (!facultyRepository.isStudentEnrolled(studentId, offeringId)) {
            throw new NotFoundException("Student " + studentId + " is not actively enrolled in offering " + offeringId + ".");
        }
    }
}
