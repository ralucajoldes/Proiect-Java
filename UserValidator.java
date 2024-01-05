package socialnetwork.domain.validators;

import socialnetwork.domain.User;
import socialnetwork.exceptions.ValidationException;

public class UserValidator implements Validator<User> {
    /**
     * Validates a given user.
     * @param entity the given User
     * @throws ValidationException when the entity is considered invalid by the given criteria
     */
    @Override
    public void validate(User entity) throws ValidationException
    {
        String exception="";
        String last_name= entity.getLastName();
        String first_name= entity.getFirstName();
        if(first_name==null||first_name.equals(""))
            exception+="Your first name cannot be emplty!\n";
        if(last_name==null||last_name.equals(""))
            exception+="Your last name cannot be emplty!\n";
        if(first_name.length()<2 || first_name.length()>20)
            exception+="Your first name must have at least 2 characters and no more than 20!\n";
        if(last_name.length()<2 || last_name.length()>20)
            exception+="Your last name must have at least 2 characters and no more than 20!\n";
        if(!first_name.matches("^[a-zA-Z0-9_-]{2,20}"))
            exception+="Your first name must not contain special symbols/characters!\n";
        if(!last_name.matches("^[a-zA-Z0-9_-]{2,20}"))
            exception+="Your last name must not contain special symbols/characters!\n";
        if(!exception.equals(""))
            throw new ValidationException(exception);
    }
}
