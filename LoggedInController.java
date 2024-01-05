package socialnetwork.controller;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import socialnetwork.domain.User;
import socialnetwork.service.CommunityService;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import socialnetwork.utils.Observer;

import java.io.File;
import java.io.FileInputStream;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;


public class LoggedInController implements Observer {

    public Label labelFullName;
    public Label labelFriends;
    public Label labelUsername;
    public ImageView img;
    public SplitPane splitPane;

    Stage stage;
    Stage current;
    User user;
    private CommunityService service;
    File chosen;
    @FXML
    Button LogOutButton;
    @FXML
    Button deleteAccountButton;
    @FXML
    Tab friendsTab;
    @FXML
    Tab composeTab;
    @FXML
    Tab newFriendsTab;
    @FXML
    Tab requestsTab;
    @FXML
    Tab inboxTab;
    @FXML
    TabPane tPane;

    public void initConnections()
    {
        try
        {
            File profile1 = new File("src/person.png");
            Image profile_image1 = new Image(profile1.toURI().toString());
            if(user.getPath()!=null && !user.getPath().equals(""))
            {
                File profile = new File(user.getPath());
                Image profile_image = new Image(profile.toURI().toString());
                img.setImage(profile_image);

                if(!profile.exists())
                    img.setImage(profile_image1);
            }
            else
                img.setImage(profile_image1);
            user = service.findUser(user);
            labelUsername.setText("Username: "+user.getUsername());
            labelUsername.setFont(Font.font("Freestyle Script", 40));
            labelFullName.setFont(Font.font("Freestyle Script", 40));
            labelFriends.setFont(Font.font("Freestyle Script", 30));
            labelFriends.setTextFill(Paint.valueOf("#20344AFF"));
            labelUsername.setTextFill(Paint.valueOf("#20344AFF"));
            labelFullName.setTextFill(Paint.valueOf("#20344AFF"));
            labelFullName.setText("Name: "+user.getFirstName()+" "+user.getLastName());
            labelFriends.setText("Number of friends: "+user.getFriends().size());

            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/view/addfriend.fxml"));
            BorderPane root = loader.load();
            Stage addStage = new Stage();
            addStage.initStyle(StageStyle.DECORATED);
            AddFriendController addFriendController = loader.getController();
            addStage.setScene(new Scene(root, 300, 300, Color.TRANSPARENT));
            addStage.setTitle("Add friends");
            addFriendController.setService(service, user);

            newFriendsTab.setContent(addStage.getScene().getRoot());
            loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/view/compose.fxml"));
            AnchorPane root2 = loader.load();
            Stage composeStage = new Stage();
            composeStage.initStyle(StageStyle.DECORATED);
            ComposeMessageController composeMessageController= loader.getController();
            composeStage.setScene(new Scene(root2, 800, 500, Color.TRANSPARENT));
            composeMessageController.setService(service, user);
            composeTab.setContent(composeStage.getScene().getRoot());




            File file5 = new File("src/add-friend.png");
            Image image5 = new Image(file5.toURI().toString());
            ImageView ivf = new ImageView();
            ivf.setImage(image5);
            ivf.setFitHeight(25);
            ivf.setFitWidth(25);
            newFriendsTab.setGraphic(ivf);

            loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/view/friends.fxml"));
            root = loader.load();
            Stage friendsStage = new Stage();
            friendsStage.initStyle(StageStyle.DECORATED);
            FriendsController friendsController = loader.getController();
            friendsStage.setScene(new Scene(root, 300, 300, Color.TRANSPARENT));
            friendsStage.setTitle("Your friends");
            friendsController.setService(service, user);
            friendsTab.setContent(friendsStage.getScene().getRoot());

            File file6 = new File("src/friends.png");
            Image image6 = new Image(file6.toURI().toString());
            ImageView ivfr = new ImageView();
            ivfr.setImage(image6);
            ivfr.setFitHeight(25);
            ivfr.setFitWidth(25);
            friendsTab.setGraphic(ivfr);

            loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/view/request.fxml"));
            root = loader.load();
            Stage requestStage = new Stage();
            requestStage.initStyle(StageStyle.DECORATED);
            RequestController requestController= loader.getController();
            requestStage.setScene(new Scene(root, 800, 500, Color.TRANSPARENT));
            requestStage.setTitle("See friend requests");
            requestController.setService(service, user);
            requestsTab.setContent(requestStage.getScene().getRoot());


            File file = new File("src/msg.png");
            Image image = new Image(file.toURI().toString());
            ImageView ivm = new ImageView();
            ivm.setImage(image);
            ivm.setFitHeight(25);
            ivm.setFitWidth(25);
            inboxTab.setGraphic(ivm);

            File file1 = new File("src/convo.png");
            Image image1 = new Image(file1.toURI().toString());
            ImageView ivc = new ImageView();
            ivc.setImage(image1);
            ivc.setFitHeight(25);
            ivc.setFitWidth(25);
            composeTab.setGraphic(ivc);

            File file3 = new File("src/friend-request.png");
            Image image3 = new Image(file3.toURI().toString());
            ImageView iva = new ImageView();
            iva.setImage(image3);
            iva.setFitHeight(30);
            iva.setFitWidth(30);
            requestsTab.setGraphic(iva);

            loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/view/inbox.fxml"));
            root = loader.load();
            Stage inboxStage = new Stage();
            inboxStage.initStyle(StageStyle.DECORATED);
            InboxController inboxController= loader.getController();
            inboxStage.setScene(new Scene(root, 800, 500, Color.TRANSPARENT));
            inboxStage.setTitle("See friend requests");
            inboxController.setService(service, user);
            inboxTab.setContent(inboxStage.getScene().getRoot());




        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
    }

    public void setService(CommunityService service, Stage stage, Stage current, User user)
    {
        this.service=service;
        this.stage=stage;
        this.current =current;
        this.user = user;
        service.add_observer(this);
        initConnections();

    }

    public void log_out()
    {
        this.stage.show();
        this.current.close();
        service.remove_observers();
    }


    public void delete_account(ActionEvent actionEvent)
    {
        try
        {
            service.deleteUser(user);
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Your account is gone!", ButtonType.OK);
            alert.setResizable(true);
            alert.showAndWait();
            this.current.close();
            this.stage.show();

        }
        catch (Exception e)
        {
            Alert alert = new Alert(Alert.AlertType.ERROR, e.getMessage(), ButtonType.OK);
            alert.setResizable(true);
            alert.show();
        }
    }


    public void change_picture(ActionEvent actionEvent)
    {
        try
        {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image file","*.png","*.jpg"));
            File file = fileChooser.showOpenDialog(this.current);
            if(file!=null)
            {
                FileInputStream stream = new FileInputStream(file.getAbsolutePath());
                img.setImage(new Image(stream));
                chosen=file;
                service.choose_picture(user,chosen.getAbsolutePath());

            }
        }
        catch (Exception e)
        {
            Alert alert = new Alert(Alert.AlertType.ERROR, e.getMessage(),ButtonType.OK);
            alert.setResizable(true);
            alert.showAndWait();
        }
    }


    @Override
    public void execute_update()
    {
        user = service.findUser(user);
        labelFriends.setText("Number of friends: "+user.getFriends().size());
    }
}
