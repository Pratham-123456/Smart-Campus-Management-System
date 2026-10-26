package com.campus.smartcampus.service;

import com.campus.smartcampus.dto.AttendanceUpdateRequest;
import com.campus.smartcampus.dto.MarksUpdateRequest;
import com.campus.smartcampus.dto.StudentAttendanceMarksDTO;
import com.campus.smartcampus.model.CourseOffering;
import com.campus.smartcampus.model.Exam;
import com.campus.smartcampus.model.Faculty;
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
                .orElseThrow(() -> new IllegalArgumentException("Faculty not found with ID: " + facultyId));
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

        facultyRepository.updateClassesHeld(offeringId, classesHeld);
    }

    @Transactional
    public void updateAttendance(Long facultyId, AttendanceUpdateRequest req) {
        validateFacultyExists(facultyId);
        validateOfferingAssignment(facultyId, req.getOfferingId());

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

        if (req.getContinuousAssessmentMarks() < 0 || req.getContinuousAssessmentMarks() > 30) {
            throw new IllegalArgumentException("Continuous Assessment marks must be between 0 and 30.");
        }
        if (req.getMidtermMarks() < 0 || req.getMidtermMarks() > 30) {
            throw new IllegalArgumentException("Midterm marks must be between 0 and 30.");
        }
        if (req.getFinalExamMarks() < 0 || req.getFinalExamMarks() > 50) {
            throw new IllegalArgumentException("Final exam marks must be between 0 and 50.");
        }

        facultyRepository.updateMarks(req.getStudentId(), req.getOfferingId(),
                req.getContinuousAssessmentMarks(), req.getMidtermMarks(), req.getFinalExamMarks());
    }

    public List<Exam> getExams(Long facultyId) {
        validateFacultyExists(facultyId);
        return facultyRepository.getExamsForFaculty(facultyId);
    }

    private void validateFacultyExists(Long facultyId) {
        if (!adminRepository.facultyExists(facultyId)) {
            throw new IllegalArgumentException("Faculty member does not exist with ID: " + facultyId);
        }
    }

    private void validateOfferingAssignment(Long facultyId, Long offeringId) {
        if (!facultyRepository.isAssignedToOffering(facultyId, offeringId)) {
            throw new IllegalStateException("Course offering " + offeringId + " is not assigned to faculty " + facultyId);
        }
    }
}
