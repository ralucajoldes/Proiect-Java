package socialnetwork.domain.validators;

import socialnetwork.exceptions.ValidationException;

public interface Validator<T> {
    void validate(T entity) throws ValidationException;
}