package by.bk.controller;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

/**
 * @author Sergey Koval
 */
@Controller
public class UIController {
    @Value("classpath:mobile-app/android/bookkeeper-1.3.11.apk")
    private Resource androidApplication;

    @RequestMapping(value = "/**/{path:[^\\.]+}", method = RequestMethod.GET)
    public String redirect() {
        return "forward:/";
    }

    @RequestMapping(value = "/mobile-app/android", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<byte[]> getAndroidMobileApplication() throws IOException {
        return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType("application/vnd.android.package-archive"))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + androidApplication.getFilename() + "\"")
                        .body(IOUtils.toByteArray(androidApplication.getInputStream()));
    }
}
