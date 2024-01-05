package socialnetwork.service;
import socialnetwork.domain.*;
import socialnetwork.exceptions.AlreadyExistsException;
import socialnetwork.exceptions.NotExistingException;
import socialnetwork.exceptions.ValidationException;
import socialnetwork.repository.PaginatedRepository;
import socialnetwork.utils.Communities;
import socialnetwork.utils.ObservableClass;
import socialnetwork.utils.Observer;
import java.util.*;
import java.util.stream.Collectors;

public class CommunityService extends ObservableClass {
    private PaginatedRepository<Long, User> repository;
    private PaginatedRepository<Long, Message> repositorym;
    private PaginatedRepository<Long, FriendRequest> repositoryr;
    private PaginatedRepository<Tuple<Long,Long>, Friendship> repositoryf;

    public CommunityService(PaginatedRepository<Long, User> repository, PaginatedRepository<Long, Message> repositorym, PaginatedRepository<Long, FriendRequest> repositoryr, PaginatedRepository<Tuple<Long, Long>, Friendship> repositoryf) {
        this.repository = repository;
        this.repositorym = repositorym;
        this.repositoryr = repositoryr;
        this.repositoryf = repositoryf;
    }

    public User getUserById(Long Id)
    {
        return repository.findOne(Id);
    }

    public User addUser(User user)
    {
        User u;
        try
        {
             u= repository.save(user);
        }
        catch(ValidationException e)
        {
            throw new ValidationException(e.getMessage());
        }
        catch (IllegalArgumentException e)
        {
            throw new IllegalArgumentException(e.getMessage());
        }
        if(u!=null) throw new AlreadyExistsException("This account already exists!");
        return u;
    }

    public User deleteUser(User user)
    {
        User me=getUserByUsername(user.getUsername());

        for(User x : me.getFriends())
        {
           this.deleteFriendship(me,x);
        }
        for(FriendRequest f: restoreRequests())
            if(f.getTo().getId().equals(me.getId())||f.getFrom().getId().equals(me.getId()))
                repositoryr.delete(f.getId());

        Iterable<Message> messages = restoreMessages();

        for(Message x: messages)
            if(x.getFrom().getId().equals(me.getId())||(x.getTo().contains(me)&&x.getTo().size()==1))
                repositorym.delete(x.getId());
            else if(x.getTo().contains(me)&&x.getTo().size()!=1)
            {
                Message message = new Message(x.getMessage(),x.getFrom());
                message.addReceiver(me);
                message.setId(x.getId());
                repositorym.update(message);
            }

        User deleted=repository.delete(me.getId());
        return deleted;
    }

    public Friendship addFriendship(User me, User friend)
    {
        Friendship f= new Friendship(me.getId(), friend.getId());
        f=repositoryf.save(f);
        notify_observer();
        return f;
    }

    public Friendship deleteFriendship(User me, User friend)
    {
        User Friend=getUserByUsername(friend.getUsername());
        User Me=getUserByUsername( me.getUsername());
        if(Friend==null)
            throw new NotExistingException("You cannot delete this friendship because your friend does not exist!Try another one!");
        if(repositoryf.findOne(new Tuple(Friend.getId(),Me.getId()))==null && repositoryf.findOne(new Tuple(Me.getId(),Friend.getId()))==null)
            throw new AlreadyExistsException("You and "+friend.getFirstName()+" are not friends!");
        Friendship f= repositoryf.delete(new Tuple(Me.getId(),Friend.getId()));
        if(f==null ) return null;
        FriendRequest friendRequest = find_friend_request(Friend.getId(), Me.getId());
        FriendRequest friendRequest1 = find_friend_request(Me.getId(), Friend.getId());
        if(friendRequest==null&&friendRequest1==null) throw new NotExistingException("This friendship does not exist!");
        if(friendRequest!=null)
                {
                if(repositoryr.delete(friendRequest.getId())==null)throw new NotExistingException("The friend request does not exist!");
                }
        if(friendRequest1!=null)
                {
                if(repositoryr.delete(friendRequest1.getId())==null)throw new NotExistingException("The friend request does not exist!");
                }
        notify_observer();
        return f;
    }

    public ArrayList<FriendDateDTO> getFriendshipsByUserReports(User user)
    {
        Long idUser= user.getId();
        ArrayList<Friendship> friendships=new ArrayList<>();
        repositoryf.findAll().forEach(f->friendships.add(f));
        ArrayList<FriendDateDTO> friendDataDTOList = (ArrayList<FriendDateDTO>) friendships.stream()
                .filter(f->f.getId().getLeft().equals(idUser))
                .map(f->new FriendDateDTO(repository.findOne(f.getId().getRight()),f.getDate()))
                .collect(Collectors.toList());
        friendDataDTOList.addAll(friendships.stream()
                .filter(f->f.getId().getRight().equals(idUser))
                .map(f->new FriendDateDTO(repository.findOne(f.getId().getLeft()),f.getDate()))
                .collect(Collectors.toList()));
        friendDataDTOList.sort(Comparator.comparing(x->x.getUserName()));
        return friendDataDTOList;
    }


    public ArrayList<FriendDateDTO> getFriendshipsByMonth(User user, int month, int year)
    {
        ArrayList<FriendDateDTO> friendships= getFriendshipsByUser(user);
        return (ArrayList<FriendDateDTO>) friendships.stream()
                .filter(fr->fr.getDate().getMonthValue()==month&&fr.getDate().getYear()==year)
                .collect(Collectors.toList());
    }

    public ArrayList<FriendDateDTO> getFriendshipsByUser(User me)
    {
        Long id_me=getUserByUsername(me.getUsername()).getId();
        ArrayList<Friendship> friendships=new ArrayList<>();
        repositoryf.getCurrentPage(me).forEach(f->friendships.add(f));
        ArrayList<FriendDateDTO>friendDateDTOS=(ArrayList<FriendDateDTO>)friendships.stream()
                                  .filter(f->f.getId().getLeft().equals(id_me))
                                  .map(f->new FriendDateDTO(getUserById(f.getId().getRight()),f.getDate()))
                                  .collect(Collectors.toList());
        friendDateDTOS.addAll(friendships.stream()
                .filter(f->f.getId().getRight().equals(id_me))
                .map(f->new FriendDateDTO(getUserById(f.getId().getLeft()),f.getDate()))
                .collect(Collectors.toList()));
        friendDateDTOS.sort(Comparator.comparing(x->x.getUserName()));
        return  friendDateDTOS;
    }
    public ArrayList<FriendDateDTO> getFriendshipsByUserNext(User me)
    {
        Long id_me=getUserByUsername(me.getUsername()).getId();
        ArrayList<Friendship> friendships=new ArrayList<>();
        Iterable<Friendship> friendshipIterable=repositoryf.nextPage(me);
        if(friendshipIterable==null)return null;
       friendshipIterable.forEach(f->friendships.add(f));
        ArrayList<FriendDateDTO>friendDateDTOS=(ArrayList<FriendDateDTO>)friendships.stream()
                .filter(f->f.getId().getLeft().equals(id_me))
                .map(f->new FriendDateDTO(getUserById(f.getId().getRight()),f.getDate()))
                .collect(Collectors.toList());
        friendDateDTOS.addAll(friendships.stream()
                .filter(f->f.getId().getRight().equals(id_me))
                .map(f->new FriendDateDTO(getUserById(f.getId().getLeft()),f.getDate()))
                .collect(Collectors.toList()));
        friendDateDTOS.sort(Comparator.comparing(x->x.getUserName()));
        return  friendDateDTOS;
    }
    public ArrayList<FriendDateDTO> getFriendshipsByUserPrevious(User me)
    {
        Long id_me=getUserByUsername(me.getUsername()).getId();
        ArrayList<Friendship> friendships=new ArrayList<>();
        Iterable<Friendship> friendshipIterable=repositoryf.previousPage(me);
        if(friendshipIterable==null)return null;
        friendshipIterable.forEach(f->friendships.add(f));
        ArrayList<FriendDateDTO>friendDateDTOS=(ArrayList<FriendDateDTO>)friendships.stream()
                .filter(f->f.getId().getLeft().equals(id_me))
                .map(f->new FriendDateDTO(getUserById(f.getId().getRight()),f.getDate()))
                .collect(Collectors.toList());
        friendDateDTOS.addAll(friendships.stream()
                .filter(f->f.getId().getRight().equals(id_me))
                .map(f->new FriendDateDTO(getUserById(f.getId().getLeft()),f.getDate()))
                .collect(Collectors.toList()));
        friendDateDTOS.sort(Comparator.comparing(x->x.getUserName()));
        return  friendDateDTOS;
    }


    public Iterable<User> getAll()
    {
        return repository.findAll();
    }
    public Iterable<User> getFirstPage(User user)
    {
        return repository.getCurrentPage(user);
    }
    public Iterable<User> getNextPage(User user)
    {
        return repository.nextPage(user);
    }
    public Iterable<User> getPreviousPage(User user)
    {
        return repository.previousPage(user);
    }

    public void setPage(Integer page)
    {
        repository.setPage(page);
        repositoryf.setPage(page);
        repositoryr.setPage(page);
        repositorym.setPage(page);
    }


    public Iterable<FriendRequest> getFirstRequests(User me)
    {
        Iterable<FriendRequest>friendRequests=repositoryr.getCurrentPage(me);
        if(friendRequests==null)return null;
        for(FriendRequest f: friendRequests)
        {
            User from = this.getUserById(f.getFrom().getId());
            User to = this.getUserById(f.getTo().getId());
            f.setFrom(from);
            f.setTo(to);
        }
      return friendRequests;
    }
    public Iterable<FriendRequest> getNextRequests(User me)
    {
        Iterable<FriendRequest>friendRequests=repositoryr.nextPage(me);
        if(friendRequests==null)return null;
        for(FriendRequest f: friendRequests)
        {
            User from = this.getUserById(f.getFrom().getId());
            User to = this.getUserById(f.getTo().getId());
            f.setFrom(from);
            f.setTo(to);
        }
        return friendRequests;
    }

    public Iterable<FriendRequest> getPreviousRequests(User me)
    {
        Iterable<FriendRequest>friendRequests=repositoryr.previousPage(me);
        if(friendRequests==null)return null;
        for(FriendRequest f: friendRequests)
        {
            User from = this.getUserById(f.getFrom().getId());
            User to = this.getUserById(f.getTo().getId());
            f.setFrom(from);
            f.setTo(to);
        }
        return friendRequests;
    }

    public Iterable<Message> getFirstMessages(User me)
    {
        Iterable<Message>messageIterable=repositorym.getCurrentPage(me);
        if(messageIterable==null)return null;
        for(Message m:messageIterable)
        {
            User from =this.getUserById(m.getFrom().getId()) ;
            List<User> toList = new ArrayList<>();
            for(User u : m.getTo())
            {
                User found = this.getUserById(u.getId());
                toList.add(found);
            }
            m.setTo(toList);
            m.setFrom(from);
        }
        return messageIterable;
    }
    public Iterable<Message> getNextMessages(User me)
    {
        Iterable<Message>messageIterable=repositorym.nextPage(me);
        if(messageIterable==null)return null;
        for(Message m:messageIterable)
        {
            User from =this.getUserById(m.getFrom().getId()) ;
            List<User> toList = new ArrayList<>();
            for(User u : m.getTo())
            {
                User found = this.getUserById(u.getId());
                toList.add(found);
            }
            m.setTo(toList);
            m.setFrom(from);
        }
        return messageIterable;
    }

    public Iterable<Message> getPreviousMessages(User me)
    {
        Iterable<Message>messageIterable=repositorym.previousPage(me);
        if(messageIterable==null)return null;
        for(Message m:messageIterable)
        {
            User from =this.getUserById(m.getFrom().getId()) ;
            List<User> toList = new ArrayList<>();
            for(User u : m.getTo())
            {
                User found = this.getUserById(u.getId());
                toList.add(found);
            }
            m.setTo(toList);
            m.setFrom(from);
        }
        return messageIterable;
    }


    public int get_communities()
    {
        Communities communities = new Communities();
        Iterable<User> users =restoreFriendships();
        return communities.connectedComponents(users);
    }

    public ArrayList<User> most_sociable_community()
    {
        Communities communities = new Communities();
        Iterable<User> users =restoreFriendships();
        return communities.sociable_community(users);
    }

    public User findUser(User u)
    {
        for(User user:restoreFriendships())
            if(user.equals(u))
                return user;
            return null;
    }
    public Iterable<User> restoreFriendships()
    {
        Iterable<User> users = repository.findAll();
        for(Friendship f : repositoryf.findAll())
        {
            User friend1=repository.findOne(f.getId().getLeft());
            User friend2=repository.findOne(f.getId().getRight());
            for(User x : users)
            {
                if (x.equals(friend1)) friend1 = x;
                    else if (x.equals(friend2)) friend2 = x;
            }
            friend1.addFriend(friend2);
            friend2.addFriend(friend1);
        }
        return users;
    }

    public Message sendMessage(Message m,ArrayList<User> receivers)
    {

     for(User x:receivers)
        {
            x=getUserByUsername(x.getUsername());
            if (x == null)
                throw new NotExistingException("Your friend " + x.getLastName() + " " + x.getFirstName() + " does not exist so you cannot message him!");
            else
                m.addReceiver(x);
        }
        Message mes;
        try {

            mes=  repositorym.save(m);
        }
        catch(IllegalArgumentException e)
        {
            return null;
        }
        if(mes!=null) throw new IllegalArgumentException("Something stopped us from sending your message!Please try again!");
        notify_observer();
        return mes;

    }

    public Iterable<Message> restoreMessages()
    {
        Iterable<Message> messages = repositorym.findAll();
        for(Message m:messages)
        {
            User from = getUserById(m.getFrom().getId());
            ArrayList<User> to = (ArrayList<User>) m.getTo().stream().map(user -> getUserById(user.getId())).collect(Collectors.toList());
            m.setFrom(from);
            m.setTo(to);
        }
        return messages;
    }

    public Iterable<Message> see_your_message(User me)
    {
        List<Message> my_messages=new ArrayList<>();
        for(Message x : restoreMessages())
            if (x.getFrom().getId().equals(me.getId())||x.getTo().stream().anyMatch(m -> m.getId().equals(me.getId())))
                 my_messages.add(x);

        return my_messages;
    }
    public Iterable<Message> see_messages_by_user(User me,User friend)
    {
        List<Message> my_messages=new ArrayList<>();
        for(Message x : restoreMessages())
            if ((x.getFrom().getId().equals(me.getId())&&x.getTo().stream().anyMatch(m -> m.getId().equals(friend.getId())))||(x.getFrom().getId().equals(friend.getId())&&x.getTo().stream().anyMatch(m -> m.getId().equals(me.getId()))))
                my_messages.add(x);
        return my_messages;
    }

   public Iterable<FriendRequest> restoreRequests()
    {

        Iterable<FriendRequest> friendRequests = repositoryr.findAll();
        for(FriendRequest r: friendRequests)
        {
            r.setFrom(repository.findOne(r.getFrom().getId()));
            r.setTo(repository.findOne(r.getTo().getId()));
        }
        return friendRequests;
    }



    public Iterable<Message> show_conversation(User me, User friend)
    {
        List<Message> my_messages=new ArrayList<>();
        for(Message x : see_your_message(me))
            if (x.getFrom().getId().equals(friend.getId())||x.getTo().stream().anyMatch(m -> m.getId().equals(friend.getId())))
                my_messages.add(x);
        Comparator<Message> byDate = new Comparator<Message>()
        {
            public int compare(Message m1, Message m2)
            {
                return m1.getDate().compareTo(m2.getDate());
            }
        };
        my_messages.sort(byDate);
        return my_messages;
    }

    public FriendRequest find_friend_request(Long id1,Long id2)
    {
        for(FriendRequest f : repositoryr.findAll())
            if((f.getFrom().getId().equals(id1)&&f.getTo().getId().equals(id2))) return f;
        return null;
    }



    public FriendRequest sendFriendRequest(User me,User friend)
    {

        if (me.equals(friend))
            throw new IllegalArgumentException("You cannot be your own friend!That's sad!:(");

        User Friend = getUserByUsername(friend.getUsername());
        User Me = getUserByUsername(me.getUsername());

        if (Friend == null)
            throw new NotExistingException("Your friend does not exist!Try another one!");

        if (repositoryf.findOne(new Tuple(Friend.getId(), Me.getId())) != null || repositoryf.findOne(new Tuple(Me.getId(), Friend.getId())) != null)
            throw new AlreadyExistsException("You and " + friend.getFirstName() + " are already friends!");

        FriendRequest r = find_friend_request(Me.getId(), Friend.getId());
        FriendRequest r1 = find_friend_request(Friend.getId(), Me.getId());

        if (r1 == null && r == null)
        {
            FriendRequest f = new FriendRequest(Me, Friend, RequestStatus.PENDING);
            f= repositoryr.save(f);
            notify_observer();
            return f;
        }

        else if (r != null && r.getStatus().equals(RequestStatus.REJECTED))
        {
            r.setStatus(RequestStatus.PENDING);
            r=repositoryr.update(r);
            notify_observer();
            return r;
        }

        else if (r1 != null && r1.getStatus().equals(RequestStatus.REJECTED))
        {
            r1.setFrom(Me);
            r1.setTo(Friend);
            r1.setStatus(RequestStatus.PENDING);
            r1=repositoryr.update(r1);
            notify_observer();
            return r1;
        }

        else if (r1 != null && r1.getStatus().equals(RequestStatus.PENDING))
            throw new AlreadyExistsException(friend.getFirstName()+" sent you a friend request" + ", but there is in pending!");

        else if (r != null && r.getStatus().equals(RequestStatus.PENDING))
            throw new AlreadyExistsException("You already sent a friend request to " + friend.getFirstName() + ", but there is in pending!");

        return null;
    }

    public FriendRequest deleteFriendRequest(User me, User friend)
    {

        FriendRequest f=find_friend_request(me.getId(), friend.getId());
        if(f==null)f=find_friend_request(friend.getId(), me.getId());
        f=repositoryr.delete(f.getId());
        notify_observer();
        return f;
    }

    public Friendship acceptFriendRequest(User me, User friend, RequestStatus status)
    {

        if(me.equals(friend))
            throw new IllegalArgumentException("You cannot accept a request from yourself!");

        User Me=getUserByUsername( me.getUsername());
        User Friend=getUserByUsername(friend.getUsername());

        if(Friend==null)
            throw new NotExistingException("Your friend does not exist!");

        FriendRequest r = find_friend_request(Me.getId(),Friend.getId());
        FriendRequest r1 = find_friend_request(Friend.getId(),Me.getId());
        if(repositoryf.findOne(new Tuple(Friend.getId(),Me.getId()))!=null||repositoryf.findOne(new Tuple(Me.getId(),Friend.getId()))!=null)
            throw new AlreadyExistsException("You and "+friend.getFirstName()+" are already friends!");

        if(r!=null&&r1==null&&r.getStatus().equals(RequestStatus.REJECTED))throw new IllegalArgumentException("This request has already been rejected!If you want to try again send another one!");

        if(r!=null&&r1==null)throw new IllegalArgumentException("You cannot accept a request that you sent!Your request is in pending for now!");

        if(r==null&&r1==null)throw new IllegalArgumentException("There is no friend request between you and "+friend.getFirstName()+" !");

        if(r1.getStatus().equals(RequestStatus.REJECTED))
            throw new AlreadyExistsException("This request has already been rejected!If you changed your mind you can send one to your friend!");
        r1.setStatus(status);
        repositoryr.update(r1);
        if (status.equals(RequestStatus.APPROVED)) return addFriendship(Me, Friend);
        notify_observer();
        return null;
    }

    public User getUserByUsername(String username)
    {
        for(User u : repository.findAll())
        { if(u.getUsername().equals(username))
                return u;
        }
        return null;
    }


    public void choose_picture(User user, String absolutePath)
    {
        user.setPath(absolutePath);
        repository.update(user);
    }



}
