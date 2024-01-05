package socialnetwork.domain.validators;

import socialnetwork.domain.FriendRequest;
import socialnetwork.domain.Message;
import socialnetwork.exceptions.ValidationException;

public class FriendRequestValidator implements Validator<FriendRequest>
{
    public void validate(FriendRequest entity) throws ValidationException
    {
        String exception="";
        if(entity.getDate()==null)
            exception+="The date cannot be null!";
        if(entity.getStatus()==null)
            exception+="The status cannot be null!";
        if(entity.getTo()==null)
            exception+="The receiver cannot be null!";
        if(entity.getFrom()==null)
            exception+="The sender cannot be null!";
        if(!exception.equals(""))
            throw new ValidationException(exception);
    }

}
