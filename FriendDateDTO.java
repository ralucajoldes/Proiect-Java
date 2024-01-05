package socialnetwork.domain;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FriendDateDTO
{
    private User user;
    private LocalDateTime date;

    public FriendDateDTO(User user, LocalDateTime date)
    {
        this.user = user;
        this.date = date;
    }

    public String getFirstName()
    {
        return user.getFirstName();
    }
    public String getUserName()
    {
        return user.getUsername();
    }
    public String getLastName()
    {

        return user.getLastName();
    }

    public User getUser() {
        return user;
    }
    public LocalDateTime getDate() {
        return date;
    }
    public String getDateConverted()
    {
        return date.format(DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm"));
    }
    @Override
    public String toString() {
        return user.getUsername()+"|"+user.getFirstName()+"|"+user.getLastName()+"|"+this.date;
    }
}
