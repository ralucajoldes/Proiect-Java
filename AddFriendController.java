package socialnetwork.controller;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import socialnetwork.domain.User;
import socialnetwork.service.CommunityService;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class AddFriendController
{
    public Button nextPage;
    public Label pageLabel;
    public Button previousPage;
    User user;
    private CommunityService service;
    ObservableList<User> model = FXCollections.observableArrayList();


    @FXML
    TableView<User> tableView;
    @FXML
    TableColumn<User,String> tableColumnFirst;
    @FXML
    TableColumn<User,String> tableColumnLast;
    @FXML
    TableColumn<User,String> tableColumnUser;
    @FXML
    TextField searchField;

    public void setService(CommunityService service, User user)
    {
        this.service=service;
        this.user = user;
        initModel();
    }


    @FXML
    public void initialize()
    {
        tableColumnUser.setCellValueFactory(new PropertyValueFactory<User, String>("username"));
        tableColumnFirst.setCellValueFactory(new PropertyValueFactory<User, String>("firstName"));
        tableColumnLast.setCellValueFactory(new PropertyValueFactory<User, String>("lastName"));
        FilteredList<User> filteredData=new FilteredList<>(model,u->true);
        searchField.textProperty().addListener((observable,oldValue,newValue)->
                {
            filteredData.setPredicate(user ->
            {
                if(newValue==null||newValue.isEmpty())
                    return true;
                else return user.getLastName().contains(newValue) || user.getFirstName().contains(newValue)||user.getUsername().contains(newValue);
            });
                }
        );
        SortedList<User> sortedList=new SortedList<>(filteredData);
        tableView.setItems(sortedList);
    }

    private void initModel()
    {
        Iterable<User> users = service.getFirstPage(user);
        List<User> userList = StreamSupport.stream(users.spliterator(), false)
                .collect(Collectors.toList());
        model.setAll(userList);
    }

    public void send_request(ActionEvent actionEvent)
    {
        User u = service.getUserByUsername(tableView.getSelectionModel().getSelectedItem().getUsername());
        try {

            if(service.sendFriendRequest(user,u)==null)
            {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Your request was successfully sent!",ButtonType.OK);
                alert.setResizable(true);
                alert.showAndWait();

            }
            else
                {
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Something went wrong! Please try again!",ButtonType.OK);
                alert.setResizable(true);
                alert.showAndWait();

            }

        }
        catch (Exception e)
        {
            Alert alert = new Alert(Alert.AlertType.ERROR,e.getMessage(), ButtonType.OK);
            alert.setResizable(true);
            alert.showAndWait();
        }
    }

    public void go_next(ActionEvent actionEvent)
    {
        Iterable<User> userIterable = service.getNextPage(user);
        if(userIterable!=null)
        {
            List<User> userList = StreamSupport.stream(userIterable.spliterator(), false)
                    .collect(Collectors.toList());
            model.setAll(userList);
            pageLabel.setText(String.valueOf(Integer.parseInt(pageLabel.getText()) + 1));
        }
    }

    public void go_previous(ActionEvent actionEvent)
    {
        Iterable<User> userIterable = service.getPreviousPage(user);
        if(userIterable!=null)
        {
            List<User> userList = StreamSupport.stream(userIterable.spliterator(), false)
                    .collect(Collectors.toList());
            model.setAll(userList);
            pageLabel.setText(String.valueOf(Integer.parseInt(pageLabel.getText()) - 1));
        }
    }
}
