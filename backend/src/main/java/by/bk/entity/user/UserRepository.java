package by.bk.entity.user;

import by.bk.entity.user.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Optional;

/**
 * @author Sergey Koval
 */
public interface UserRepository extends MongoRepository<User, String> {
    @Query(value = "{_id: ?0}", fields = "{password: 1, email: 0}")
    User getUserPassword(String login);
    @Query(value = "{_id: ?0}", fields = "{email: 1, password: 1, roles: 1}")
    Optional<User> authenticateUser(String login);
    @Query(value = "{_id: ?0}", fields = "{email: 0, roles: 1}")
    Optional<User> getAuthenticatedUser(String login);
}