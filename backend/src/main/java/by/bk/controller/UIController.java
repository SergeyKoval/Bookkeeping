package by.bk.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * @author Sergey Koval
 */
@Controller
public class UIController {
    @Value("classpath:mobile-app/android")
    private Resource androidFolder;

    @RequestMapping(value = "/**/{path:[^\\.]+}", method = RequestMethod.GET)
    public String redirect() {
        return "forward:/";
    }

    @RequestMapping(value = "/mobile-app/android", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<byte[]> getAndroidMobileApplication() throws IOException {
        File application = androidFolder.getFile().listFiles()[0];
        return application != null && application.isFile()
                ? ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType("application/vnd.android.package-archive"))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + application.getName() + "\"")
                        .body(Files.readAllBytes(application.toPath()))
                : ResponseEntity.notFound().build();
    }
}
