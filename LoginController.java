package socialnetwork.controller;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import socialnetwork.domain.Password;
import socialnetwork.domain.User;
import socialnetwork.service.CommunityService;

import java.io.File;

public class LoginController
{


    public ImageView imV;
    Stage stage;
    Stage current;
    private CommunityService service;

    @FXML
    TextField FirstNameField;
    @FXML
    TextField LastNameField;
    public TextField UsernameField;
    public javafx.scene.control.PasswordField PasswordField;

    @FXML
    Button SignUpButton;
    public Button CancelButton;

    public void setService(CommunityService service, Stage current, Stage stage)
    {
        this.service=service;
        this.stage=stage;
        this.current=current;
        File file = new File("src/logo.png");
        Image image1 = new Image(file.toURI().toString());
        imV.setImage(image1);
    }


    public void sign_up(ActionEvent actionEvent)
    {

        String username =UsernameField.getText();
        String password= PasswordField.getText();
        String firstname= FirstNameField.getText();
        String lastname= LastNameField.getText();

        UsernameField.clear();
        PasswordField.clear();
        FirstNameField.clear();
        LastNameField.clear();

        try
        {
            User userToAdd=new User(firstname,lastname,username, Password.getSaltedHash(password));
            userToAdd.setPath("");
            service.addUser(userToAdd);
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION,"Your account has been successfully created!", ButtonType.OK);
            alert.setResizable(true);
            alert.show();
            this.current.hide();
            this.stage.show();

        }
        catch(Exception ex)
        {
            Alert alert = new Alert(Alert.AlertType.ERROR,ex.getMessage(), ButtonType.OK);
            alert.setResizable(true);
            alert.show();
        }
    }


    public void cancel(ActionEvent actionEvent)
    {
       this.current.hide();
       this.stage.show();
    }
}
