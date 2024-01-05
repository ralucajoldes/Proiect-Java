package socialnetwork.domain.validators;

import socialnetwork.domain.Message;
import socialnetwork.domain.User;
import socialnetwork.exceptions.ValidationException;

public class MessageValidator implements Validator<Message>
{
    public void validate(Message entity) throws ValidationException
    {
        String exception="";
        if(entity.getDate()==null)
            exception+="The date cannot be null!";
        if(entity.getMessage()==null||entity.getMessage().equals(""))
            exception+="The message cannot be null or empty string!";
        if(entity.getTo()==null)
            exception+="The receiver cannot be null!";
        if(entity.getFrom()==null)
            exception+="The sender cannot be null!";
        if(!exception.equals(""))
            throw new ValidationException(exception);
    }

}
