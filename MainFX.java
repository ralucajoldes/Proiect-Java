package socialnetwork;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import socialnetwork.config.ApplicationContext;
import socialnetwork.controller.AuthentificationController;
import socialnetwork.domain.*;
import socialnetwork.domain.validators.*;
import socialnetwork.repository.PaginatedRepository;
import socialnetwork.repository.database.*;
import socialnetwork.service.CommunityService;


public class MainFX extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception
    {

        final String url = ApplicationContext.getPROPERTIES().getProperty("database.socialnetwork.url");
        final String username= ApplicationContext.getPROPERTIES().getProperty("databse.socialnetwork.username");
        final String password= ApplicationContext.getPROPERTIES().getProperty("database.socialnetwork.password");
        Integer pageSize=10;
        PaginatedRepository<Long, User> userRepository = new UsersDbRepository(url,username, password,  new UserValidator(),pageSize);
        PaginatedRepository<Tuple<Long,Long>, Friendship> friendshipsRepository = new FriendshipsDbRepository(url,username, password,  new FriendshipValidator(),pageSize);
        PaginatedRepository<Long, Message> messagesRepository = new MessageDbRepository(url,username, password,  new MessageValidator(),pageSize);
        PaginatedRepository<Long, FriendRequest> friendRequestRepository = new FriendRequestDbRepository(url,username, password,  new FriendRequestValidator(),pageSize);
        CommunityService communityService=new CommunityService(userRepository,messagesRepository,friendRequestRepository,friendshipsRepository);
        FXMLLoader loader=new FXMLLoader();
        loader.setLocation(getClass().getResource("/view/authentification.fxml"));
        BorderPane root=loader.load();
        primaryStage.initStyle(StageStyle.DECORATED);
        AuthentificationController authentificationController = loader.getController();
        primaryStage.setScene(new Scene(root, 700, 500, Color.TRANSPARENT));
        authentificationController.setService(communityService,primaryStage);
        primaryStage.show();


    }

    public static void main(String[] args) {
        launch(args);
    }

}
