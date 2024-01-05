package socialnetwork.controller;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import socialnetwork.domain.Message;
import socialnetwork.domain.User;
import socialnetwork.service.CommunityService;
import java.util.ArrayList;


public class ComposeMessageController
{
    public javafx.scene.control.TextField textField;
    public Button previousPage;
    public Label pageLabel;
    public Button nextPage;
    User user;

    private CommunityService service;
    ObservableList<Message> model = FXCollections.observableArrayList();

    @FXML
    VBox vBox;


    ArrayList<CheckBox> vCheck=new ArrayList<>();

    public void setService(CommunityService service, User user)
    {
        this.service=service;
        this.user = user;
        loadPage(service.getFirstPage(user));
    }

    public boolean loadPage(Iterable<User>userIterable)
    {
        if(userIterable==null) return false;
        else
        {
            vBox.getChildren().clear();
            Label lb = new Label("Choose a user to send a message to:");
            ArrayList<String> str = new ArrayList<>();
            for (User x : userIterable) {
                String n = "";
                n = x.getUsername();
                str.add(n);
            }
            vBox.setSpacing(5);
            lb.setFont(Font.font("Century Gothic", 22));
            lb.setTextFill(Paint.valueOf("#20344A"));
            vBox.getChildren().add(lb);
            for (String s : str)
            {
                CheckBox cb = new CheckBox();
                Label new_label = new Label(s);
                cb.setGraphic(new_label);
                new_label.setFont(Font.font("Century Gothic",20));
                new_label.setTextFill(Paint.valueOf("#09118D"));
                vBox.getChildren().add(cb);
                Label l1 = new Label(s + " not selected");
                l1.setFont(Font.font("Century Gothic"));
                l1.setTextFill(Paint.valueOf("slategray"));
                EventHandler<ActionEvent> event = new EventHandler<ActionEvent>() {

                    public void handle(ActionEvent e) {
                        if (cb.isSelected()) {
                            l1.setText(s + " selected ");
                            vCheck.add(cb);
                        } else {
                            l1.setText(s + " not selected ");
                            vCheck.remove(cb);
                        }
                    }

                };
                cb.setOnAction(event);
                vBox.getChildren().add(l1);
            }

        }
        return true;
    }

    public void send_message(ActionEvent actionEvent)
    {
        ArrayList<User> to = new ArrayList<>();
        String message = textField.getText();
        for(CheckBox c : vCheck)
        {
            User x = service.getUserByUsername(((Label)c.getGraphic()).getText());
            to.add(x);
        }
        Message m =new Message(message,user);
        service.sendMessage(m,to);
        textField.setText("");
        vCheck.forEach(checkBox -> checkBox.setSelected(false));
    }

    public void go_previous(ActionEvent actionEvent)
    {

       if(loadPage(service.getPreviousPage(user))) pageLabel.setText(String.valueOf(Integer.parseInt(pageLabel.getText()) - 1));
    }

    public void go_next(ActionEvent actionEvent)
    {
      if(loadPage(service.getNextPage(user)))  pageLabel.setText(String.valueOf(Integer.parseInt(pageLabel.getText()) + 1));
    }
}
