package com.unison.billgeneration.controller;

import com.unison.billgeneration.service.BillGenerationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.FileNotFoundException;
import java.io.IOException;

@RestController
public class BillGenerationController {

    @Autowired
    private BillGenerationService billGenerationService;

    @PutMapping("/emp")
    public ResponseEntity<Resource> generateBillForEmp(MultipartHttpServletRequest request) throws IOException, FileNotFoundException {
        System.out.println("In controller");
        String projName = request.getParameter("projName");
        String clients = request.getParameter("clients");
        MultipartFile file = request.getFile("file");
        return billGenerationService.generateBill(projName, clients, file);
    }
}


