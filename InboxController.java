package socialnetwork.controller;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import socialnetwork.domain.Message;
import socialnetwork.domain.User;
import socialnetwork.service.CommunityService;
import socialnetwork.utils.Observer;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class InboxController implements Observer
{

    public Button previousPage;
    public Label pageLabel;
    public Button nextPage;
    User user;
    private CommunityService service;
    ObservableList<Message> model = FXCollections.observableArrayList();


    @FXML
    TableView<Message> tableView;
    @FXML
    TableColumn<Message,String> tableColumnMessage;
    @FXML
    TableColumn<Message,String> tableColumnFrom;
    @FXML
    TableColumn<Message,String> tableColumnTo;
    @FXML
    TableColumn<Message,String> tableColumnDate;
    @FXML
    TextField searchField;
    @FXML
    TextField tField;
    public void setService(CommunityService service, User user)
    {
        this.service=service;
        this.user = user;
        service.add_observer(this);
        initModel();
    }


    @FXML
    public void initialize()
    {

        tableColumnMessage.setCellValueFactory(new PropertyValueFactory<Message, String>("message"));
        tableColumnFrom.setCellValueFactory(new PropertyValueFactory<Message, String>("fromUser"));
        tableColumnTo.setCellValueFactory(new PropertyValueFactory<Message, String>("toUser"));
        tableColumnDate.setCellValueFactory(new PropertyValueFactory<Message, String>("dateConverted"));
        FilteredList<Message> filteredData=new FilteredList<Message>(model,u->true);
        searchField.textProperty().addListener((observable,oldValue,newValue)->
                {
                    filteredData.setPredicate(message ->
                    {
                        if(newValue==null||newValue.isEmpty())
                            return true;
                        else return message.getMessage().contains(newValue) || message.getFrom().getUsername().contains(newValue);
                    });
                }
        );
        SortedList<Message> sortedList=new SortedList<>(filteredData);
        tableView.setItems(sortedList);
    }

    private void initModel()
    {
        Iterable<Message> messages = service.getFirstMessages(user);
        List<Message> messageList = StreamSupport.stream(messages.spliterator(), false)
                .collect(Collectors.toList());
        model.setAll(messageList);
    }

    public void send_message(ActionEvent actionEvent)
    {
        String message = tField.getText();
        try
        {

            Message m = new Message(message,user);
            String to = tableView.getSelectionModel().getSelectedItem().getFromUser();
            User userTo = service.getUserByUsername(to);
            ArrayList<User> toUsers=new ArrayList<User>();
            toUsers.add(userTo);
            service.sendMessage(m,toUsers);


        }
        catch (Exception e)
        {
            Alert alert = new Alert(Alert.AlertType.ERROR, e.getMessage(),ButtonType.OK);
            alert.setResizable(true);
            alert.showAndWait();
        }
    }

    public void go_previous(ActionEvent actionEvent)
    {
        Iterable<Message> messages = service.getPreviousMessages(user);
        if(messages!=null)
        {
            List<Message> messageList = StreamSupport.stream(messages.spliterator(), false)
                    .collect(Collectors.toList());
            model.setAll(messageList);
            pageLabel.setText(String.valueOf(Integer.parseInt(pageLabel.getText()) - 1));

        }
    }

    public void go_next(ActionEvent actionEvent)
    {
        Iterable<Message> messages = service.getNextMessages(user);
        if(messages!=null)
        {
            List<Message> messageList = StreamSupport.stream(messages.spliterator(), false)
                    .collect(Collectors.toList());
            model.setAll(messageList);
            pageLabel.setText(String.valueOf(Integer.parseInt(pageLabel.getText()) + 1));

        }
    }

    @Override
    public void execute_update()
    {
        model.removeAll();
        initModel();
        tField.setText("");

    }
}
