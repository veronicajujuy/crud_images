package com.vvaldez.imagesuploaddownload.controllers;

import com.vvaldez.imagesuploaddownload.services.IS3Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
public class S3Controller {
    private IS3Service s3Service;

    public S3Controller(IS3Service s3Service) {
        this.s3Service = s3Service;
    }

    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file")MultipartFile file) throws IOException {
        return s3Service.uploadFile(file);
    }

    @GetMapping("/download/{fileName}")
    public String downloadFile(@PathVariable("fileName") String fileName) throws IOException {
        return s3Service.downloadFile(fileName);
    }

    @GetMapping("/list")
    public List<String> getAllObjects() throws IOException {
        return s3Service.listFiles();
    }

    @PutMapping("/{oldFileName}/{newFileName}")
    public String renameFile(@PathVariable("oldFileName") String oldFileName,
                             @PathVariable("newFileName") String newFileName
    ) throws IOException {
        return s3Service.renameFile(oldFileName, newFileName);
    }

    @PutMapping("/update/{oldFileName}")
    public String updateFile(@RequestParam("file")MultipartFile file,
                             @PathVariable("oldFileName") String oldFileName
    ) throws IOException {
        return s3Service.updateFile(file, oldFileName);
    }

    @DeleteMapping("/delete/{fileName}")
    public String deleteFile(@PathVariable("fileName") String fileName) throws IOException {
        return s3Service.deleteFile(fileName);
    }

}
