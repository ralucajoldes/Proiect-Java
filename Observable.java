package socialnetwork.utils;

public interface Observable
{
    void add_observer(Observer o);
    void notify_observer();
    void remove_observers();
}
