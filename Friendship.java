package socialnetwork.domain;

import java.time.LocalDateTime;
import java.util.Objects;


public class Friendship extends Entity<Tuple<Long,Long>> {

    LocalDateTime date;

    /**
     * Creates a Friendship.
     * @param Id1 a long that cannot be null
     * @param Id2 a long that cannot be null
     */
    public Friendship(Long Id1, Long Id2)
    {
        setId(new Tuple<>(Id1,Id2));
        this.date= LocalDateTime.now();
    }

    /**
     *
     * @return the date when the friendship was created
     */
    public LocalDateTime getDate() {
        return date;
    }

    /**
     * Sets the date for a Friendship.
     * @param date a date that represents the start of our Friendship
     */
    public void setDate(LocalDateTime date)
    {
        this.date=date;
    }
    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof Friendship)) return false;
        Friendship that = (Friendship) o;
        return (getId().getRight().equals(that.getId().getRight()) &&
                getId().getLeft().equals(that.getId().getLeft()))||(getId().getLeft().equals(that.getId().getRight()) &&
                getId().getRight().equals(that.getId().getLeft()));
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId().getLeft(), getId().getRight());
    }
}
