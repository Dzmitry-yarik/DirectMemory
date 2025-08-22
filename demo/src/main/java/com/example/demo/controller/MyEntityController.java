package com.example.demo.controller;

import com.example.demo.service.MyEntityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.IOException;

@RequiredArgsConstructor
@RestController
public class MyEntityController {
    private final MyEntityService service;

    @PostMapping("/file")
    public ResponseEntity<String> addedFileInVersion(MultipartHttpServletRequest request) throws IOException {
        return service.addFileVersion(request);
    }
}

