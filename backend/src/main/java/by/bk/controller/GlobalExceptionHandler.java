package by.bk.controller;

import org.apache.catalina.connector.ClientAbortException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;
import org.springframework.web.multipart.MultipartException;

/**
 * Global exception handler for handling client disconnection exceptions.
 *
 * These exceptions occur when clients disconnect before the server finishes
 * sending a response. They are normal occurrences and should not be treated
 * as application errors.
 *
 * @author Sergey Koval
 */
@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Log LOG = LogFactory.getLog(GlobalExceptionHandler.class);

    /**
     * Handles AsyncRequestNotUsableException thrown when trying to write to
     * an async request that has been closed by the client.
     */
    @ExceptionHandler(AsyncRequestNotUsableException.class)
    public void handleAsyncRequestNotUsable(AsyncRequestNotUsableException ex) {
        LOG.debug("Client disconnected before response could be sent: " + ex.getMessage());
    }

    /**
     * Handles ClientAbortException thrown when the client aborts the connection
     * (typically due to timeout or user navigation).
     */
    @ExceptionHandler(ClientAbortException.class)
    public void handleClientAbort(ClientAbortException ex) {
        LOG.debug("Client aborted connection: " + ex.getMessage());
    }

    /**
     * Handles MultipartException thrown when parsing malformed multipart requests.
     * This commonly occurs from spam/bot traffic sending invalid multipart/form-data
     * requests. Since this API doesn't use multipart file uploads, these are
     * typically malicious or malformed requests that should be rejected.
     */
    @ExceptionHandler(MultipartException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public void handleMultipartException(MultipartException ex) {
        LOG.debug("Failed to parse multipart request (likely spam): " + ex.getMessage());
    }
}
