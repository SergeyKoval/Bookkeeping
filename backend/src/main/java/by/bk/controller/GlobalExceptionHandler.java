package by.bk.controller;

import org.apache.catalina.connector.ClientAbortException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;

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
}
