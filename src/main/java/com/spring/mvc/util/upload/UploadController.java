package com.spring.mvc.util.upload;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
@Slf4j
public class UploadController {

    @GetMapping("/upload-file")
    public String uploadForm(){
        return "upload/upload-form";
    }
    @PostMapping("/upload-file")
    public String uploadForm(@RequestParam("thumbnail") MultipartFile file){
        log.info("file-name {}",file.getOriginalFilename());
        log.info("file-size {}",file.getSize()/1024.0);
        log.info("file-type {}",file.getContentType());

        return  "";
    }
}
