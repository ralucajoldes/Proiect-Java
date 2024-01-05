package socialnetwork.domain;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class FriendRequest extends Entity<Long>
{
    private User to;
    private User from;
    private LocalDateTime date;
    private RequestStatus status;

    public FriendRequest(User from,User to, RequestStatus status)
    {

        this.from = from;
        this.to = to;
        this.status = status;
    }
    public String getFirstName()
    {
        return from.getFirstName();
    }
    public String getLastName()
    {
        return from.getLastName();
    }

    public String getToName()
    {
        return to.getUsername();
    }
    public String getFromName()
    {
        return from.getUsername();
    }

    public User getFrom() {
        return from;
    }
    public LocalDateTime getDate() {
        return date;
    }
    public User getTo() {
        return to;
    }
    public RequestStatus getStatus() {
        return status;
    }

    public void setTo(User to) {
        this.to = to;
    }

    public void setFrom(User from) {
        this.from = from;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }

    public String getDateConverted()
    {
        return date.format(DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm"));
    }

    @Override
    public String toString() {
        return "FriendRequest{" +
                "to=" + to +
                ", from=" + from +
                ", date=" + date +
                ", status=" + status +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FriendRequest that = (FriendRequest) o;
        return ((FriendRequest) o).getId().equals(this.getId());
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(getId());
    }
}
