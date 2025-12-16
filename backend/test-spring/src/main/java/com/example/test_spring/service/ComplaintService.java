package com.example.test_spring.service;

import com.example.test_spring.model.Complaint;
import com.example.test_spring.repository.ComplaintRepository;
import com.example.test_spring.security.InputSanitizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ComplaintService {

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private InputSanitizer inputSanitizer;

    public List<Complaint> getAllComplaints() {
        return complaintRepository.findAll();
    }

    public List<Complaint> getComplaintsByUser(String userEmail) {
        return complaintRepository.findByUserEmailOrderByCreatedAtDesc(userEmail);
    }

    public Optional<Complaint> getComplaintById(String complaintId) {
        return complaintRepository.findById(complaintId);
    }

    public Complaint createComplaint(Complaint complaint) {
        // Sanitize inputs
        complaint.setIssueType(inputSanitizer.sanitizeHtml(complaint.getIssueType()));
        complaint.setDescription(inputSanitizer.sanitizeComplaintText(complaint.getDescription()));
        complaint.setOrderId(inputSanitizer.sanitizeHtml(complaint.getOrderId()));
        
        complaint.setCreatedAt(LocalDateTime.now());
        complaint.setUpdatedAt(LocalDateTime.now());
        complaint.setStatus("PENDING");
        
        return complaintRepository.save(complaint);
    }

    public Complaint updateComplaint(String complaintId, Complaint updatedComplaint) {
        Optional<Complaint> existingComplaint = complaintRepository.findById(complaintId);
        
        if (existingComplaint.isPresent()) {
            Complaint complaint = existingComplaint.get();
            
            if (updatedComplaint.getStatus() != null) {
                complaint.setStatus(inputSanitizer.sanitizeHtml(updatedComplaint.getStatus()));
            }
            if (updatedComplaint.getDescription() != null) {
                complaint.setDescription(inputSanitizer.sanitizeComplaintText(updatedComplaint.getDescription()));
            }
            if (updatedComplaint.getAssignedTo() != null) {
                complaint.setAssignedTo(inputSanitizer.sanitizeHtml(updatedComplaint.getAssignedTo()));
            }
            
            complaint.setUpdatedAt(LocalDateTime.now());
            return complaintRepository.save(complaint);
        }
        
        return null;
    }

    public boolean deleteComplaint(String complaintId) {
        if (complaintRepository.existsById(complaintId)) {
            complaintRepository.deleteById(complaintId);
            return true;
        }
        return false;
    }

    public List<Complaint> searchComplaints(String userEmail, String status, String issueType) {
        if (status != null && issueType != null) {
            return complaintRepository.findByUserEmailAndStatusAndIssueType(userEmail, status, issueType);
        } else if (status != null) {
            return complaintRepository.findByUserEmailAndStatus(userEmail, status);
        } else if (issueType != null) {
            return complaintRepository.findByUserEmailAndIssueType(userEmail, issueType);
        }
        return getComplaintsByUser(userEmail);
    }
}