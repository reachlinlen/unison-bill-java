package com.unison.billgeneration.controller;

import com.unison.billgeneration.service.EmpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.*;
import java.time.LocalDate;

@RestController
public class EmpController {

    @Autowired
    private EmpService empService;

    @GetMapping("/emp")
    public ResponseEntity<Resource> generateBillForEmp(String empName, String startDate, String endDate, String clientName, String fileName) throws IOException, FileNotFoundException {
//        System.out.println(empName);
//        System.out.println(startDate);
//        System.out.println(endDate);
//        System.out.println(clientName);
//        System.out.println(LocalDate.parse(startDate));
        return empService.generateBill(empName, LocalDate.parse(startDate), LocalDate.parse(endDate), clientName, fileName);
    }

    @GetMapping("/empExcel")
    public void generateBillForEmps(MultipartHttpServletRequest request) {
          MultipartFile file = request.getFile("file");
    }
}
