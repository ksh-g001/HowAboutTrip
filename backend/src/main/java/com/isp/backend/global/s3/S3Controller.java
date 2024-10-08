package com.isp.backend.global.s3;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class S3Controller {

    private final S3Service s3Service;

    /** 테스트용 사진 업로드 - url 리턴 **/
    @PostMapping(value = "/upload/country", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public String uploadImage(@RequestPart MultipartFile country) throws IOException {
        return s3Service.uploadImage(BucketDir.IMAGE, country);
    }

}