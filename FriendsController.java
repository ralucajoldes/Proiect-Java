package socialnetwork.controller;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import socialnetwork.domain.FriendDateDTO;
import socialnetwork.domain.User;
import socialnetwork.service.CommunityService;
import socialnetwork.utils.Observer;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class FriendsController implements Observer
{

    public Label pageLabel;

    User user;
    private CommunityService service;
    ObservableList<FriendDateDTO> model = FXCollections.observableArrayList();

    @FXML
    TableView<FriendDateDTO> tableView;
    @FXML
    TableColumn<FriendDateDTO,String> tableColumnFirst;
    @FXML
    TableColumn<FriendDateDTO,String> tableColumnLast;
    @FXML
    TableColumn<FriendDateDTO,String> tableColumnDate;
    @FXML
    TableColumn<FriendDateDTO,String> tableColumnUsername;

    @FXML
    TextField searchField;
    @FXML
    Button deleteButton;
    @FXML
    Button nextPage;
    @FXML
    Button previousPage;

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
        tableColumnUsername.setCellValueFactory(new PropertyValueFactory<FriendDateDTO, String>("userName"));
        tableColumnFirst.setCellValueFactory(new PropertyValueFactory<FriendDateDTO, String>("firstName"));
        tableColumnLast.setCellValueFactory(new PropertyValueFactory<FriendDateDTO, String>("lastName"));
        tableColumnDate.setCellValueFactory(new PropertyValueFactory<FriendDateDTO, String>("dateConverted"));
        FilteredList<FriendDateDTO> filteredData=new FilteredList<>(model, u->true);
        searchField.textProperty().addListener((observable,oldValue,newValue)->
                {
                    filteredData.setPredicate(friendDateDTO ->
                    {
                        if(newValue==null||newValue.isEmpty())
                            return true;
                        else if(friendDateDTO.getFirstName().contains(newValue)||friendDateDTO.getLastName().contains(newValue))
                            return true;
                        else return false;
                    });
                }
        );
        SortedList<FriendDateDTO> sortedList=new SortedList<>(filteredData);
        tableView.setItems(sortedList);
    }

    private void initModel()
    {

        Iterable<FriendDateDTO> friendDateDTOS = service.getFriendshipsByUser(user);
        List<FriendDateDTO> friendDateDTOList = StreamSupport.stream(friendDateDTOS.spliterator(), false)
                .collect(Collectors.toList());
        model.setAll(friendDateDTOList);

    }

    public void delete_friend(ActionEvent actionEvent)
    {
        try {
            User friend = new User("","",tableView.getSelectionModel().getSelectedItem().getUserName(),"");

            if (service.deleteFriendship(user, friend) != null)
            {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Your friend has been deleted!",ButtonType.OK);
                alert.setResizable(true);
                alert.showAndWait();
                execute_update();
            }
            else
            {
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "You cannot delete this friendship!",ButtonType.OK);
                alert.setResizable(true);
                alert.showAndWait();

            }

        }
        catch (Exception e)
        {
            {
                Alert alert = new Alert(Alert.AlertType.ERROR, e.getMessage(),ButtonType.OK);
                alert.setResizable(true);
                alert.showAndWait();

            }
        }
    }

    public void go_next(ActionEvent actionEvent)
    {
        Iterable<FriendDateDTO> friendDateDTOS = service.getFriendshipsByUserNext(user);
        if(friendDateDTOS!=null)
        {
            List<FriendDateDTO> friendDateDTOList = StreamSupport.stream(friendDateDTOS.spliterator(), false)
                    .collect(Collectors.toList());
            model.setAll(friendDateDTOList);
            pageLabel.setText(String.valueOf(Integer.parseInt(pageLabel.getText()) + 1));
        }
    }

    public void go_previous(ActionEvent actionEvent)
    {
        Iterable<FriendDateDTO> friendDateDTOS = service.getFriendshipsByUserPrevious(user);
        if(friendDateDTOS!=null)
        {
            List<FriendDateDTO> friendDateDTOList = StreamSupport.stream(friendDateDTOS.spliterator(), false)
                    .collect(Collectors.toList());
            model.setAll(friendDateDTOList);
            pageLabel.setText(String.valueOf(Integer.parseInt(pageLabel.getText()) - 1));
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


