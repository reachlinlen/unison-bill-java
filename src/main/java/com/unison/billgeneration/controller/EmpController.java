package com.unison.billgeneration.controller;

import com.unison.billgeneration.service.EmpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.*;
import java.time.LocalDate;

@RestController
public class EmpController {

    @Autowired
    private EmpService empService;

    @PutMapping("/emp")
    public ResponseEntity<Resource> generateBillForEmp(MultipartHttpServletRequest request) throws IOException, FileNotFoundException {
        System.out.println("In controller");
        String attention = request.getParameter("attention");
        String PONum = request.getParameter("PONum");
        String projName = request.getParameter("projName");
        String costCentre = request.getParameter("costCentre");
        String account = request.getParameter("account");
        String clients = request.getParameter("clients");
        MultipartFile file = request.getFile("file");
        return empService.generateBill(attention, PONum, projName, costCentre, account, clients, file);
    }
}
