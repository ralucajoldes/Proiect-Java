package socialnetwork.controller;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import socialnetwork.domain.*;
import socialnetwork.service.CommunityService;
import socialnetwork.utils.Observer;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class RequestController implements Observer {

    public Button previousPage;
    public Label pageLabel;
    public Button nextPage;
    User user;
    private CommunityService service;

    ObservableList<FriendRequest> model = FXCollections.observableArrayList();
    @FXML
    TableView<FriendRequest> tableView;
    @FXML
    TableColumn<FriendRequest,String> tableColumnTo;
    @FXML
    TableColumn<FriendRequest,String> tableColumnFrom;
    @FXML
    TableColumn<FriendRequest,String> tableColumnDate;
    @FXML
    TableColumn<FriendRequest,String> tableColumnStatus;
    @FXML
    ChoiceBox choice_request;


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

        tableColumnTo.setCellValueFactory(new PropertyValueFactory<FriendRequest, String>("toName"));
        tableColumnFrom.setCellValueFactory(new PropertyValueFactory<FriendRequest, String>("fromName"));
        tableColumnDate.setCellValueFactory(new PropertyValueFactory<FriendRequest, String>("dateConverted"));
        tableColumnStatus.setCellValueFactory(new PropertyValueFactory<FriendRequest, String>("status"));
        tableView.setItems(model);

    }

    private void initModel()
    {
        Iterable<FriendRequest> friendRequests = service.getFirstRequests(user);
        List<FriendRequest> friendRequestList = StreamSupport.stream(friendRequests.spliterator(), false)
                .collect(Collectors.toList());
        model.setAll(friendRequestList);
    }

    public void friend_request(ActionEvent actionEvent)
    {

        try {
            String from = tableView.getSelectionModel().getSelectedItem().getFromName();
            String to = tableView.getSelectionModel().getSelectedItem().getToName();
            User toUser = service.getUserByUsername(to);
            User fromUser = service.getUserByUsername(from);
            if (toUser.equals(user))
            {
                try {
                    String c = choice_request.getValue().toString();
                    if (c.equals("ACCEPT"))
                    {

                        if (service.acceptFriendRequest(user, fromUser, RequestStatus.APPROVED) == null)
                        {
                            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Congrats!You and " + fromUser.getFirstName() + " are now friends!", ButtonType.OK);
                            alert.setResizable(true);
                            alert.showAndWait();
                        }
                        else
                            {
                            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Something went wrong! Please try again!", ButtonType.OK);
                            alert.setResizable(true);
                            alert.showAndWait();

                        }
                    } else if (c.equals("REJECT")) {
                        if (service.acceptFriendRequest(user, fromUser, RequestStatus.REJECTED) == null) {
                            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "You and " + fromUser.getFirstName() + " are not friends!", ButtonType.OK);
                            alert.setResizable(true);
                            alert.showAndWait();
                        } else {
                            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Something went wrong! Please try again!", ButtonType.OK);
                            alert.setResizable(true);
                            alert.showAndWait();

                        }
                    } else if (c.equals("CANCELED")){
                        Alert alert = new Alert(Alert.AlertType.INFORMATION, "You cannot cancel a request that was not made by you!", ButtonType.OK);
                        alert.setResizable(true);
                        alert.showAndWait();

                    }
                    else {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION, "You choose to stay in pending, so we will give you time to think about this friendship!", ButtonType.OK);
                        alert.setResizable(true);
                        alert.showAndWait();

                    }
                    execute_update();
                }

                catch (Exception e)
                    {
                        Alert alert = new Alert(Alert.AlertType.ERROR, e.getMessage(),ButtonType.OK);
                        alert.setResizable(true);
                        alert.showAndWait();
                    }

            }
            else
                if(fromUser.equals(user))
                {
                    try
                    {
                        String c = choice_request.getValue().toString();
                        if (!c.equals("CANCELED"))
                        {
                            Alert alert = new Alert(Alert.AlertType.ERROR,"A request that was made by you can only be canceled!",ButtonType.OK);
                            alert.setResizable(true);
                            alert.showAndWait();
                        }
                        else
                        {
                            service.deleteFriendRequest(fromUser,toUser);
                            Alert alert = new Alert(Alert.AlertType.CONFIRMATION,"You deleted that friend request!",ButtonType.OK);
                            alert.setResizable(true);
                            alert.showAndWait();
                        }
                        execute_update();
                    }
                    catch (Exception e)
                    {
                        Alert alert = new Alert(Alert.AlertType.ERROR, e.getMessage(),ButtonType.OK);
                        alert.setResizable(true);
                        alert.showAndWait();
                    }
                }

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
        Iterable<FriendRequest> friendRequests = service.getPreviousRequests(user);
        if(friendRequests!=null) {
            List<FriendRequest> friendRequestList = StreamSupport.stream(friendRequests.spliterator(), false)
                    .collect(Collectors.toList());
            model.setAll(friendRequestList);
            pageLabel.setText(String.valueOf(Integer.parseInt(pageLabel.getText()) - 1));
        }
    }

    public void go_next(ActionEvent actionEvent)
    {
        Iterable<FriendRequest> friendRequests = service.getNextRequests(user);
        if(friendRequests!=null) {
            List<FriendRequest> friendRequestList = StreamSupport.stream(friendRequests.spliterator(), false)
                    .collect(Collectors.toList());
            model.setAll(friendRequestList);
            pageLabel.setText(String.valueOf(Integer.parseInt(pageLabel.getText()) + 1));
        }
    }

    @Override
    public void execute_update()
    {
        service.setPage(1);
        model.removeAll();
        initModel();
        pageLabel.setText("1");
    }
}
