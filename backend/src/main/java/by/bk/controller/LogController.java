package by.bk.controller;

import by.bk.controller.model.response.SimpleResponse;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class LogController {

  private static final Log LOG = LogFactory.getLog(LogController.class);

  @Data
  @NoArgsConstructor
  public static class LogRequest {
    private String message;
  }

  @PostMapping("/logs")
  public SimpleResponse logMessage(@RequestBody LogRequest request) {
    LOG.info(request.getMessage());
    return SimpleResponse.success();
  }
}
