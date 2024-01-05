package socialnetwork.utils;

import socialnetwork.domain.User;

import java.util.ArrayList;
import java.util.List;

public class Communities {
    /**
     * It explores all the friends that can be reached from an user and prints their ids.
     * @param x a User
     * @param visited an ArrayList of users
     **/
    void DFSUtil(User x, ArrayList<User> visited)
    {

        visited.add(x);
        System.out.print(x.getId()+" ");
        for (User friend : x.getFriends())
        {
            if(!visited.contains(friend)) DFSUtil(friend,visited);
        }

    }

    /**
     *
     * @param users a list of users
     * @return returns the number of communities in our social network
     */
    public int connectedComponents(Iterable<User> users)
    {
        ArrayList<User> visited = new ArrayList<User>();
        int number_comunities=0;
        for(User x: users)
        {
            if(!visited.contains(x))
            {
                DFSUtil(x,visited);
                number_comunities++;
                System.out.println();
            }
        }
        return number_comunities;
    }

    /**
     *
     * @param users a list of users
     * @return a list of users that represent the most sociable community
     */
    public ArrayList<User> sociable_community(Iterable<User> users)
    {
        Integer number_comunities=0;
        ArrayList<User>max_path=new ArrayList<User>();

        for(User x: users)
        {
            ArrayList<User>visited=new ArrayList<User>();
            visited.add(x);
            max_comp_conex(x.getFriends(),visited,max_path);
            visited.clear();
        }
        return max_path;
    }

    /**
     *
     * @param friends a list of users
     * @param visited a list of users that represent the longest path in that community
     */
    private void max_comp_conex(List<User> friends, ArrayList<User> visited, ArrayList<User> longest_path)
    {

        ArrayList<User> remember=new ArrayList<User>(visited);
        for (User friend : friends)
        {
            if(!visited.contains(friend))
            {
                ArrayList<User>friends2=new ArrayList<User>(friend.getFriends());
                ArrayList<User>visited2=new ArrayList<>(remember);
                visited2.add(friend);
                max_comp_conex(friends2,visited2,longest_path);
                if(longest_path.size()<visited2.size())
                {
                    longest_path.clear();
                    longest_path.addAll(visited2);
                }
            }

        }

    }
}
