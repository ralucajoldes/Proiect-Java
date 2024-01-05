package socialnetwork.repository.database;

import socialnetwork.domain.*;
import socialnetwork.domain.validators.Validator;
import socialnetwork.exceptions.AlreadyExistsException;
import socialnetwork.exceptions.ValidationException;
import socialnetwork.repository.PaginatedRepository;
import socialnetwork.repository.Repository;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FriendRequestDbRepository implements PaginatedRepository<Long,FriendRequest>
{
    private String url;
    private String username;
    private String password;
    private Validator<FriendRequest> validator;
    private Integer pageSize;
    private Integer currentPage;
    @Override
    public void setPage(Integer page)
    {
        this.currentPage=page;
    }
    public FriendRequestDbRepository(String url, String username, String password, Validator<FriendRequest> validator,Integer pageSize)
    {
        this.url = url;
        this.username = username;
        this.password = password;
        this.validator = validator;
        this.pageSize = pageSize;
        this.currentPage = 1;
    }
    @Override
    public Iterable<FriendRequest> findAll()
    {
        Set<FriendRequest> friendRequests = new HashSet<>();
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("SELECT * from friend_requests");
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next())
            {
                Long id = resultSet.getLong("id");
                Long id_friend1 = resultSet.getLong("user_from");
                Long id_friend2 = resultSet.getLong("user_to");
                User from = new User(null,null,null,null);
                from.setId(id_friend1);
                User to = new User(null,null,null,null);
                to.setId(id_friend2);
                String date = resultSet.getString("date_r");
                String status = resultSet.getString("status");
                FriendRequest f= new FriendRequest(from,to,RequestStatus.valueOf(status));
                f.setDate(LocalDateTime.parse(date));
                f.setId(id);
                friendRequests.add(f);
            }
            return friendRequests;
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
        return friendRequests;
    }
    @Override
    public FriendRequest findOne(Long id)
    {
        if (id==null)
            throw new IllegalArgumentException("The id must be not null!");
        for(FriendRequest f:findAll())
            if(f.getId().equals(id))return f;
        return null;
    }

    @Override
    public FriendRequest save(FriendRequest entity)
    {

        if (entity==null)
            throw new IllegalArgumentException("Entity must be not null!");

        Long user1_id=entity.getFrom().getId();
        Long user2_id=entity.getTo().getId();
        entity.setDate(LocalDateTime.now());
        String date = entity.getDate().toString();
        String status = entity.getStatus().toString();
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("INSERT INTO friend_requests(user_from,user_to,status,date_r) VALUES (?,?,?,?)");)
        {

            statement.setLong(1,user1_id);
            statement.setLong(2,user2_id);
            statement.setString(3,status);
            statement.setString(4,date);
            validator.validate(entity);
            statement.executeUpdate();
        }
        catch (ValidationException e)
        {
            System.out.println(e.getMessage());
        }
        catch (SQLException e)
        {

            e.printStackTrace();
        }

        return null;
    }
    @Override
    public FriendRequest delete(Long id)
    {
       FriendRequest friendRequest;
        try
        {
           friendRequest = findOne(id);
        }
        catch (Exception e)
        {

            System.out.println(e.getMessage());
            return null;
        }
        if(friendRequest==null)return null;

        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("DELETE FROM friend_requests WHERE user_from=? AND user_to=? AND id=?");)
        {

            statement.setLong(1,friendRequest.getFrom().getId());
            statement.setLong(2,friendRequest.getTo().getId());
            statement.setLong(3,friendRequest.getId());
            statement.executeUpdate();
        }
        catch (SQLException e)
        {

            e.printStackTrace();
        }

        return friendRequest;

    }

    @Override
    public FriendRequest update(FriendRequest entity)
    {
        if (entity==null)
            throw new IllegalArgumentException("Entity must be not null!");
        FriendRequest friendRequest;
        try
        {
           friendRequest = findOne(entity.getId());
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
            return entity;
        }
        if(friendRequest==null)return entity;
        Long id=friendRequest.getId();
        Long user1_id=entity.getFrom().getId();
        Long user2_id=entity.getTo().getId();
        entity.setDate(LocalDateTime.now());
        String date = entity.getDate().toString();
        String status = entity.getStatus().toString();
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("UPDATE friend_requests SET date_r=?, status=?,user_from=?,user_to=? WHERE id=?");)
        {

            statement.setString(1,date);
            statement.setString(2,status);
            statement.setLong(5,id);
            statement.setLong(3,user1_id);
            statement.setLong(4,user2_id);
            validator.validate(entity);
            statement.executeUpdate();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Iterable<FriendRequest> nextPage(User user)
    {
       if(currentPage<totalPages(user))
       {
           currentPage++;
        return getCurrentPage(user);
       }
       return null;
    }



    @Override
    public Iterable<FriendRequest> previousPage(User user)
    {
        if(currentPage>1)
        {
            currentPage--;
            return getCurrentPage(user);
        }
        return null;
    }

    @Override
    public Iterable<FriendRequest> getCurrentPage(User user)
    {
        Long filter= (long) -1;
        if(user!=null)filter=user.getId();
        List<FriendRequest> friendRequests = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("SELECT * from friend_requests  where (user_to=? OR user_from=?) AND status=? order by date_r desc limit "+pageSize+" offset "+pageSize*(currentPage-1));
             ) {
            statement.setLong(1,filter);
            statement.setLong(2,filter);
            statement.setString(3,"PENDING");
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next())
            {
                Long id = resultSet.getLong("id");
                Long id_friend1 = resultSet.getLong("user_from");
                Long id_friend2 = resultSet.getLong("user_to");
                User from = new User(null,null,null,null);
                from.setId(id_friend1);
                User to = new User(null,null,null,null);
                to.setId(id_friend2);
                String date = resultSet.getString("date_r");
                String status = resultSet.getString("status");
                FriendRequest f= new FriendRequest(from,to,RequestStatus.valueOf(status));
                f.setDate(LocalDateTime.parse(date));
                f.setId(id);
                friendRequests.add(f);
            }
            return friendRequests;
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
        return friendRequests;
    }

    @Override
    public Integer getPageNumber() {
        return currentPage;
    }

    @Override
    public Integer getPageSize() {
        return pageSize;
    }

    @Override
    public Integer totalPages(User user)
    {
        Long filter= (long) -1;
        if(user!=null)filter=user.getId();
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("SELECT COUNT (*) AS total from friend_requests where (user_to=? OR user_from=?) AND status=?");
             )
        {
            statement.setLong(1,filter);
            statement.setLong(2,filter);
            statement.setString(3,"PENDING");
            ResultSet resultSet = statement.executeQuery();
            resultSet.next();
            Integer friendships=resultSet.getInt("total");
            if(friendships%pageSize!=0)
                return friendships/pageSize+1;
            else
                return friendships/pageSize;
        }
        catch (SQLException throwables)
        {
            throwables.printStackTrace();
        }
        return null;
    }
}