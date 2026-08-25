package com.kltn.school_hrm.service.implement;

import java.util.List;

import com.kltn.school_hrm.dto.request.TeachingAssignmentRequest;
import com.kltn.school_hrm.dto.response.TeachingAssignmentResponse;
import com.kltn.school_hrm.enums.Enums.Curriculum;
import com.kltn.school_hrm.service.TeachingAssignmentService;

public class TeachingAssignmentServiceImpl implements TeachingAssignmentService {

    @Override
    public TeachingAssignmentResponse create(TeachingAssignmentRequest request) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'create'");
    }

    @Override
    public TeachingAssignmentResponse update(Long id, TeachingAssignmentRequest request) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'update'");
    }

    @Override
    public TeachingAssignmentResponse getById(Long id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getById'");
    }

    @Override
    public List<TeachingAssignmentResponse> getAll() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAll'");
    }

    @Override
    public List<TeachingAssignmentResponse> getByTeacherId(Long teacherId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getByTeacherId'");
    }

    @Override
    public List<TeachingAssignmentResponse> getByTeacherIdAndCurriculum(Long teacherId, Curriculum curriculum) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getByTeacherIdAndCurriculum'");
    }

    @Override
    public void delete(Long id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'delete'");
    }

}
