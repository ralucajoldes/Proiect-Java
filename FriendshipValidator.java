package socialnetwork.domain.validators;

import socialnetwork.domain.Friendship;
import socialnetwork.exceptions.ValidationException;

public class FriendshipValidator implements Validator<Friendship> {
    /**
     * Validates a given friendship.
     * @param entity the given Friendship
     * @throws ValidationException when the entity is considered invalid by the given criteria
     */
    @Override
    public void validate(Friendship entity) throws ValidationException {
        if(entity==null||entity.getId()==null||entity.getId().getRight()==null||entity.getId().getLeft()==null)
            throw new ValidationException("This friendship is invalid!");
    }
}
