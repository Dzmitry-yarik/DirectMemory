package com.example.demo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.util.UriUtils;

import java.io.IOException;
import java.lang.management.BufferPoolMXBean;
import java.lang.management.ManagementFactory;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class MyEntityService {
    @Transactional
    public ResponseEntity<String> addFileVersion(MultipartHttpServletRequest request) throws IOException {
        try {
            log.info("Before reading : {} bytes", getDirectMemoryUsed());
            MultipartFile f = request.getFile("file");
            byte[] array = f.getBytes();
            String fileName = "1111" + f.getOriginalFilename();
            storeFile(array, fileName);
            log.info("After reading : {} bytes", getDirectMemoryUsed());
            return ResponseEntity.ok("ok");
        }finally {
            log.info("Finally reading : {} bytes", getDirectMemoryUsed());
        }
    }

    private void storeFile(byte[] array, String path) throws IOException {
        String decode = UriUtils.decode(path, StandardCharsets.UTF_8.toString());
        synchronized (this) {
            Path savedPath = Paths.get("/storage/tmp", decode);
            if (Files.notExists(savedPath.getParent()))
                Files.createDirectories(savedPath.getParent());
            Files.write(savedPath, array);
        }
    }

    private double getDirectMemoryUsed() {
        List<BufferPoolMXBean> bufferPoolMXBeans = ManagementFactory.getPlatformMXBeans(BufferPoolMXBean.class);
        for (BufferPoolMXBean pool : bufferPoolMXBeans) {
            if ("direct".equals(pool.getName())) {
                return pool.getMemoryUsed();
            }
        }
        return 0;
    }
}