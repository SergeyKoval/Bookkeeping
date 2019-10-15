package by.bk.security.role;

import by.bk.entity.user.UserPermission;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Sergey Koval
 */
@Target(value = {ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("hasAuthority(T(by.bk.entity.user.UserPermission).USER.name())")
//@PreAuthorize(value = "hasPermission(#permission, T(by.bk.entity.user.UserPermission).USER, 'read')")
public @interface RoleUser {
    UserPermission permission() default UserPermission.USER;
}
