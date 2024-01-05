package socialnetwork.controller;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import socialnetwork.domain.Password;
import socialnetwork.domain.User;
import socialnetwork.service.CommunityService;

import java.io.File;

public class AuthentificationController
{

    public ImageView imV;
    private CommunityService service;
    public TextField UsernameField;
    public javafx.scene.control.PasswordField PasswordField;
    public Button ForgotPassword;
    public Button LogInButton;
    public Button SignUpButton;
    Stage stage;

    public void setService(CommunityService service,Stage stage)
    {
        this.service=service;
        this.stage=stage;
        File file = new File("src/logo.png");
        Image image1 = new Image(file.toURI().toString());
        imV.setImage(image1);
    }


    public void log_in(ActionEvent actionEvent) throws Exception
    {
        String username= UsernameField.getText();
        String password= PasswordField.getText();
        User user= service.getUserByUsername(username);
        UsernameField.clear();
        PasswordField.clear();
        if(user==null)
        {
            Alert alert = new Alert(Alert.AlertType.ERROR,"This user was not found! Please try another one!", ButtonType.OK);
            alert.setResizable(true);
            alert.show();
            return;
        }

        else
        {
            try{
                if(!Password.check(password, user.getPassword()))
                {
                    Alert alert = new Alert(Alert.AlertType.ERROR,"Incorrect password!", ButtonType.OK);
                    alert.setResizable(true);
                    alert.show();
                    return;
                }
            }
            catch (Exception e)
            {
                Alert alert = new Alert(Alert.AlertType.ERROR,e.getMessage(), ButtonType.OK);
                alert.setResizable(true);
                alert.show();
                return;
            }
        }

            try
            {

                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(getClass().getResource("/view/loggedin.fxml"));
                BorderPane root = loader.load();
                Stage loggedInStage = new Stage();
                loggedInStage.initStyle(StageStyle.DECORATED);
                LoggedInController loggedInController = loader.getController();
                loggedInStage.setScene(new Scene(root, 1500, 700, Color.TRANSPARENT));
                loggedInController.setService(service, this.stage, loggedInStage, user);
                this.stage.hide();
                loggedInStage.show();

            } catch (Exception e)
            {
                System.out.println(e.getMessage());
            }


    }

    public void sign_up(ActionEvent actionEvent)
    {
        try
        {

            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/view/login.fxml"));
            BorderPane root = loader.load();
            Stage logInStage = new Stage();
            logInStage.initStyle(StageStyle.DECORATED);
            LoginController logInController = loader.getController();
            logInStage.setScene(new Scene(root, 700, 500, Color.TRANSPARENT));
            logInController.setService(service, logInStage,this.stage);
            this.stage.hide();
            logInStage.show();

        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }

    }
}
