package socialnetwork.domain;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Message extends Entity<Long>
{
    private String message;
    private User from;
    private List<User> to;
    private LocalDateTime date;


    public Message(String message, User from)
    {
        this.message = message;
        this.from = from;
        this.to = new ArrayList<User>();
        this.date = LocalDateTime.now();
    }
    public String getDateConverted()
    {
        return date.format(DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm"));
    }
    public String getMessage() {
        return message;
    }

    public User getFrom() {
        return from;
    }

    public String getFromUser()
    {
        return from.getUsername();
    }

    public List<User> getTo() {
        return to;
    }

    public String getToUser()
    {
        String to="";
        for(User x : getTo())
            to+=x.getUsername()+";";
        return to;
    }

    public LocalDateTime getDate() {
        return date;
    }
    public void setDate(LocalDateTime date)
    {
        this.date=date;
    }
    public void addReceiver(User u)
    {
        to.add(u);
    }
    public Long getIdMessage(){return getId();};
    public void setTo(List<User> to)
    {
        this.to = to;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message1 = (Message) o;
        return message1.getId().equals(this.getId());
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setFrom(User from) {
        this.from = from;
    }


    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    @Override
    public String toString() {
        return "Message " + getId()+ "|"+ "message:'" + message + "|" +
                "from:" + from +"|"+
                "to:" + to +"|"+
                "date:" + date;
    }
}
