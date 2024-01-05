package socialnetwork.utils;

import java.util.ArrayList;
import java.util.List;

public class ObservableClass implements Observable
{
    List<Observer> observers=new ArrayList<>();

    @Override
    public void add_observer(Observer o)
    {
        if(o==null)throw new IllegalArgumentException("Observer cannot be null!");
        observers.add(o);
    }

    @Override
    public void notify_observer()
    {
        observers.forEach(Observer::execute_update);
    }

    @Override
    public void remove_observers()
    {
        observers.clear();
    }

}
