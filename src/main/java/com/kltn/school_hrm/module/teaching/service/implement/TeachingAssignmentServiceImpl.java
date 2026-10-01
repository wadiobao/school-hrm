package com.kltn.school_hrm.module.teaching.service.implement;

import java.util.List;

import org.springframework.stereotype.Service;

import com.kltn.school_hrm.module.teaching.dto.request.TeachingAssignmentRequest;
import com.kltn.school_hrm.module.teaching.dto.response.TeachingAssignmentResponse;
import com.kltn.school_hrm.shared.enums.Enums.Curriculum;
import com.kltn.school_hrm.module.teaching.service.TeachingAssignmentService;

@Service
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
