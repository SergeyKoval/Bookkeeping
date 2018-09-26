package by.bk.security;

import lombok.AllArgsConstructor;
import lombok.ToString;

/**
 * @author Sergey Koval
 */
@ToString
@AllArgsConstructor
public class MissedUserException extends RuntimeException {
    private String email;
}