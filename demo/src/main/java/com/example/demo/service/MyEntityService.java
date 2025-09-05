package com.example.demo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.util.UriUtils;

import java.io.File;
import java.io.IOException;
import java.lang.management.BufferPoolMXBean;
import java.lang.management.ManagementFactory;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@Slf4j
@RequiredArgsConstructor
public class MyEntityService {

    private static final String STORAGE_PATH = "/storage/tmp/";
    private static final String POST_URL = "http://localhost:8088/file";

    public ResponseEntity<String> downloadAndAddMultipart() {
        for (int fileSize = 400; fileSize <= 800; fileSize += 100) {
            byte[] fileBytes = generateFileContent(fileSize * 1024 * 1024);

            for (int i = 0; i < 10; i++) {
                downloadAndProcessFile(String.valueOf(fileSize), String.valueOf(i), fileBytes);
            }
        }
        return ResponseEntity.ok("ok");
    }

    public void downloadAndProcessFile(String size, String iteration, byte[] fileBytes) {
        System.out.println(size + " " + iteration);
        RestClient restClient = RestClient.create();
        try {
            File tempFile = File.createTempFile(size + "_" + iteration + "_", ".zip");
            Files.write(tempFile.toPath(), fileBytes);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new FileSystemResource(tempFile));

            restClient.post()
                    .uri(POST_URL)
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(body)
                    .retrieve()
                    .toEntity(String.class);

            tempFile.delete();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static byte[] generateFileContent(int sizeInBytes) {
        byte[] fileContent = new byte[sizeInBytes];
        ThreadLocalRandom.current().nextBytes(fileContent);
        return fileContent;
    }

    @Transactional
    public ResponseEntity<String> addFileVersion(MultipartHttpServletRequest request) throws IOException {
        try {
            log.info("Before reading : {} bytes", getDirectMemoryUsed());
            MultipartFile f = request.getFile("file");
            byte[] array = f.getBytes();
            String fileName = STORAGE_PATH + f.getOriginalFilename();
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
            Path savedPath = Paths.get(decode);
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