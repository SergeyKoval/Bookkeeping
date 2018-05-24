package by.bk.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Sergey Koval
 */
@RestController
@RequestMapping("/api")
public abstract class BaseAPIController {
    protected final Log LOG = LogFactory.getLog(this.getClass());
}