package TheBookArchive;


import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.control.TextArea;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.TableCell;
import javafx.stage.FileChooser;
import javafx.geometry.Pos;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Alert;
import javafx.stage.Modality;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.Region;
import javafx.scene.text.Font;
import javafx.stage.Screen;



import javafx.geometry.Insets;
//import javafx.geometry.Rectangle2D;

import java.util.ArrayList;

import java.io.File;
import java.io.IOException;
import java.util.Optional;



import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.nio.file.*;
import java.nio.channels.FileLock;
import java.nio.channels.FileChannel;
import java.io.RandomAccessFile;




public class App extends Application {
    private ArrayList<Table> tableArrList = new ArrayList<Table>(); //contains user entered name and settings
    private TableView primaryTable = new TableView();
    String currBookList = "-Select a Book List-";
    int currBookListIndex = -1;
    private MenuButton primaryMenuButton = new MenuButton(currBookList);
    FileLock lock;
    private String connectionUrl = "jdbc:sqlserver://DESKTOP-560E1RE:1433;database=bookdb;integratedSecurity=true;encrypt=false";
    

    /**
     * This method refreshes the table shown in the primary Stage. It also is used to change shown columns
     * @param tableName currently viewed table. The name is in the format stored in the database
     */
    public void refreshTable(String tableName){
        try (Connection connection = DriverManager.getConnection(connectionUrl);) {
                Statement statement = connection.createStatement();
                String sqlQuery = "SELECT * FROM " + tableName;
                ResultSet resultSet = statement.executeQuery(sqlQuery);
                primaryTable.getItems().clear();
                while(resultSet.next())
                    primaryTable.getItems().add(new Book(resultSet.getString(2), resultSet.getString(3), resultSet.getString(4), resultSet.getString(5), resultSet.getString(6), resultSet.getString(7), resultSet.getString(8), resultSet.getString(9), resultSet.getString(10)));
                
                resultSet = statement.executeQuery("SELECT * FROM _SETTINGS_ WHERE tableNameDB='" + tableName + "';");
                resultSet.next();
                if(primaryTable.getItems().isEmpty()){
                    Label placeHolderLabel = new Label(resultSet.getString(2) + " is empty");
                    placeHolderLabel.setFont(new Font(30));
                    primaryTable.setPlaceholder(placeHolderLabel);   
                    
                }
                ObservableList columns = primaryTable.getColumns();
                for(int i = 1; i < 9; i++){
                    TableColumn<Book, String> tableColumn = (TableColumn<Book,String>) primaryTable.getColumns().get(i);
                    tableColumn.setVisible(resultSet.getBoolean(i + 3));
                }
        }
        catch(SQLException e){
            e.printStackTrace();
        }
    }

    /**
     * This method updates the currentBookList, currentBookListDB, and currBookListIndex variables
     * @param bookList current booklist in the user entered format
     */
    public void updateCurrBookLists(String bookList){
        currBookList = bookList;
        for(int i = 0; i < tableArrList.size(); i++){
            if(bookList.equals(tableArrList.get(i).getName())){
                currBookListIndex = i;
                break;
            }
        }
    }

    /**
     * This method updates the primaryMenuButton. This is called at the start of the program, and when a table is created or deleted
     */
    public void refreshPrimaryMenuButton(){
        primaryMenuButton.getItems().clear();
        for(int i = 0; i < tableArrList.size(); i++){
            MenuItem menuItem = new MenuItem(tableArrList.get(i).getName());
            menuItem.setOnAction(event -> {
                updateCurrBookLists(menuItem.getText());
                primaryMenuButton.setText(currBookList);
                refreshTable(tableArrList.get(currBookListIndex).getNameInDB());
            });
            primaryMenuButton.getItems().add(menuItem);
        }
        primaryMenuButton.setText(currBookList);
    }

    public boolean BITToBool(String BIT){
        if(BIT.equals("1")){
            return true;
        }
        return false;
    }

    public String boolToBit(boolean bool){
        if(bool){
            return "1";
        }
        return "0";
    }

    /**
     * Displays an informational alert to the user
     * @param errMsg error message
     */
    public void showAlert(String errMsg){
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setContentText(errMsg);
        alert.show();
    }

    /**
     * Displays an error alert to the user
     * @param errMsg error message
     */
    public void showError(String errMsg){
        Alert alert = new Alert(AlertType.ERROR);
        alert.setContentText(errMsg);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.show();
    }
    
    /**
     * This method adds a new book to the currently viewed list
     * @param primaryStage The primary stage
     */
    public void addBook(Stage primaryStage){
            if(currBookListIndex == -1){
                showAlert("Please select a book list.");
            }
            else{
                File selectedFile[] = {null};
                Stage secondaryStage = new Stage();
                secondaryStage.initModality(Modality.WINDOW_MODAL);
                secondaryStage.initOwner(primaryStage);
                secondaryStage.setWidth(505);;
                secondaryStage.setHeight(685);
                secondaryStage.setResizable(false);
                secondaryStage.setTitle("Add Book");
                secondaryStage.getIcons().add(new Image("file:testingjavafx\\src\\main\\resources\\projectImages\\Bookcase.png"));
                Label titleLabel = new Label("Title");
                TextArea titleTextArea = new TextArea();
                Label authorLabel = new Label("Author");
                TextArea authorTextArea = new TextArea();
                Label publishDateLabel = new Label("Year Published");
                TextArea publishTextArea = new TextArea();
                Label numPagesLabel = new Label("Number of Pages");
                TextArea numPagesTextArea = new TextArea();
                Label isbnLabel = new Label("isbn 13");
                TextArea isbnTextArea = new TextArea();
                Label startDateLabel = new Label("Started Reading (YYYY-MM-DD)");
                TextArea startDateTextArea = new TextArea();
                Label endDateLabel = new Label("Finished Reading (YYYY-MM-DD)");
                TextArea endDateTextArea = new TextArea();
                Label ratingLabel = new Label("Rating (0.0 - 10.0)");
                TextArea ratingTextArea = new TextArea();
                Label coverLabel = new Label("Cover Art");
                Button save = new Button("Save");
                Button cancel = new Button("Cancel");
                Button selectFileButton = new Button("Select Image");
                Button removeFileButton = new Button("Remove Image");
                Label selectedFileLabel = new Label();
                HBox selectedFileHBox = new HBox(selectFileButton, selectedFileLabel);
                selectedFileHBox.setSpacing(5);
                VBox titleVBox = new VBox(titleLabel, titleTextArea);
                VBox authorVBox = new VBox(authorLabel, authorTextArea);
                VBox publishDateVBox = new VBox(publishDateLabel, publishTextArea);
                VBox numPagesVBox = new VBox(numPagesLabel, numPagesTextArea);
                VBox isbnVBox = new VBox(isbnLabel, isbnTextArea);
                VBox startDateVBox = new VBox(startDateLabel, startDateTextArea);
                VBox endDateVBox = new VBox(endDateLabel, endDateTextArea);
                VBox ratingVBox = new VBox(ratingLabel, ratingTextArea);
                VBox fileButtonVBox =  new VBox(selectedFileHBox, removeFileButton);
                fileButtonVBox.setSpacing(5);
                VBox fileVBox = new VBox(coverLabel, fileButtonVBox);
                HBox exitHBox = new HBox(save, cancel);
                exitHBox.setSpacing(5);
                VBox addBookVBox = new VBox(titleVBox, authorVBox, publishDateVBox, numPagesVBox, isbnVBox, startDateVBox, endDateVBox, ratingVBox, fileVBox, exitHBox);
                addBookVBox.setSpacing(10);
                addBookVBox.setPadding(new Insets(5));
                Scene addBookScene = new Scene(addBookVBox);
                secondaryStage.setScene(addBookScene);

                titleTextArea.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
                    if (e.getCode() == KeyCode.ENTER) 
                        e.consume();
                    
                    if (e.getCode() == KeyCode.DOWN) {
                        authorTextArea.requestFocus();
                    }
                    
                });
                titleTextArea.setMaxHeight(15);

                authorTextArea.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
                    if (e.getCode() == KeyCode.ENTER) 
                        e.consume(); 

                    if (e.getCode() == KeyCode.DOWN) 
                        publishTextArea.requestFocus(); 

                    if (e.getCode() == KeyCode.UP)
                        titleTextArea.requestFocus(); 
                    
                });
                authorTextArea.setMaxHeight(15);

                publishTextArea.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
                    if (e.getCode() == KeyCode.ENTER) 
                        e.consume();
                    
                    if (e.getCode() == KeyCode.DOWN) 
                        numPagesTextArea.requestFocus();
                    
                    if (e.getCode() == KeyCode.UP) 
                        authorTextArea.requestFocus(); 
                    
                });
                publishTextArea.setMaxHeight(15);

                numPagesTextArea.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
                    if (e.getCode() == KeyCode.ENTER) 
                        e.consume(); 

                    if (e.getCode() == KeyCode.DOWN)
                        isbnTextArea.requestFocus();
                    
                    if (e.getCode() == KeyCode.UP)
                        publishTextArea.requestFocus();
                });
                numPagesTextArea.setMaxHeight(15);

                isbnTextArea.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
                    if (e.getCode() == KeyCode.ENTER) 
                        e.consume();
                    
                    if (e.getCode() == KeyCode.DOWN) 
                        startDateTextArea.requestFocus();
                    
                    if (e.getCode() == KeyCode.UP) 
                        numPagesTextArea.requestFocus();
                    
                });
                isbnTextArea.setMaxHeight(15);

                startDateTextArea.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
                    if (e.getCode() == KeyCode.ENTER) 
                        e.consume(); 
                    
                    if (e.getCode() == KeyCode.DOWN) 
                        endDateTextArea.requestFocus(); 
                    
                    if (e.getCode() == KeyCode.UP) 
                        isbnTextArea.requestFocus();
                });
                startDateTextArea.setMaxHeight(15);

                endDateTextArea.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
                    if (e.getCode() == KeyCode.ENTER) 
                        e.consume();
                    
                    if (e.getCode() == KeyCode.DOWN) 
                        ratingTextArea.requestFocus();
                    
                    if (e.getCode() == KeyCode.UP) 
                        startDateTextArea.requestFocus();
                });
                endDateTextArea.setMaxHeight(15);

                ratingTextArea.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
                    if (e.getCode() == KeyCode.ENTER) 
                        e.consume();
                    
                    if (e.getCode() == KeyCode.UP) 
                        endDateTextArea.requestFocus(); 
                });
                ratingTextArea.setMaxHeight(15);
 
                FileChooser fileChooser = new FileChooser();
                FileChooser.ExtensionFilter imageFilter = 
                    new FileChooser.ExtensionFilter("Image Files (*.png, *.jpg, *jpeg)", "*.png", "*.jpg", "*.jpeg");
                fileChooser.getExtensionFilters().add(imageFilter);
                selectFileButton.setOnAction(Event -> {
                    selectedFile[0] = fileChooser.showOpenDialog(secondaryStage);
                    selectedFileLabel.setText(selectedFile[0].getName());
                });

                removeFileButton.setOnAction(even -> {
                    selectedFile[0] = null;
                    selectedFileLabel.setText("");
                });
                
                save.setOnAction(Event -> { 
                    if(titleTextArea.getText().equals("")){
                        showAlert("Every book requires a title.");
                        
                    }
                    else{
                        String coverPath = null;
                        if(selectedFile[0] != null && selectedFile[0].exists()){
                            coverPath = selectedFile[0].getAbsolutePath();
                            Path source = Paths.get(coverPath);
                            Path destination = Paths.get("testingjavafx\\src\\main\\resources\\coverImages\\" + titleTextArea.getText() + "-" + authorTextArea.getText() + selectedFile[0].toString().substring(selectedFile[0].toString().lastIndexOf('.')));
                            File destinationFile = destination.toFile();

                            if(destinationFile.exists()){ 
                                String destinationPathStr = destination.toString();
                                int fileTypeIndex = destinationFile.toString().lastIndexOf('.');
                                int copyNum = 0;
                                boolean foundAvailableName = true;
                                while(foundAvailableName){
                                    copyNum++;
                                    destination = Paths.get(destinationPathStr.substring(0, fileTypeIndex) + " (" + copyNum + ")" + destinationPathStr.substring(fileTypeIndex));
                                    destinationFile = destination.toFile();
                                    if(!destinationFile.exists())
                                        foundAvailableName = false;
                                }

                                try{
                                    coverPath = (Files.copy(source, destination)).toString();

                                }
                                catch(IOException e){
                                    e.printStackTrace();
                                }
                            }
                            else{
                                try{
                                    coverPath = (Files.copy(source, destination)).toString();
                                }
                                catch(IOException e){
                                    e.printStackTrace();
                                    
                                }
                            }
                        }
                        
                        try (Connection connection = DriverManager.getConnection(connectionUrl);) {
                            String sqlCommand = "INSERT INTO " + tableArrList.get(currBookListIndex).getNameInDB() + " (title, author, yearPublished, numPages, isbn, startDate, endDate, rating, coverPath) VALUES ('";
                            
                            String parameter = titleTextArea.getText();
                            if(parameter.length() > 100)
                                throw new Exception("Title is greater than 100 characters. Abandoning save operation.");
                            else
                                sqlCommand += parameter + "', '";

                            parameter = authorTextArea.getText();
                            if(parameter.length() > 50)
                                throw new Exception("Author is greater than 50 characters. Abandoning save operation.");
                            else    
                                sqlCommand += parameter + "',";

                            parameter = publishTextArea.getText();
                            if(parameter.equals(""))
                                sqlCommand += " " + null + ",";
                            else{
                                try{
                                    int numParam = Integer.parseInt(parameter);
                                    sqlCommand += " '" + numParam + "',";
                                }
                                catch(NumberFormatException e){
                                    throw new Exception("A non-numerical charcter was entered in the Year Published text area. Abandoning save operation.");
                                }
                            }
                            parameter = numPagesTextArea.getText();
                            if(parameter.equals(""))
                                sqlCommand += " " + null + ", '";
                            else{
                                try{
                                    int numParam = Integer.parseInt(parameter);
                                    sqlCommand += " '" + numParam + "', '";
                                }
                                catch(NumberFormatException e){
                                    throw new Exception("A non-numerical charcter was entered in the Number of Pages text area. Abandoning save operation.");
                                    
                                }
                            }
                            parameter = isbnTextArea.getText();
                            if(parameter.equals(""))
                                sqlCommand += parameter + "',";
                            else if(parameter.length() > 17)    
                                throw new Exception("ISBN-13 has an incorrect number of characters. Abandoning save operation");
                            else{
                                sqlCommand += parameter + "',";
                            }
                            parameter = startDateTextArea.getText();
                            if(parameter.equals(""))
                                sqlCommand += " " + null + ",";
                            else if(parameter.length() != 10)
                                throw new Exception("Start Date has an incorrect number of characters. Abandoning save operation");     
                            else{
                                if(parameter.charAt(4) == '-' && parameter.charAt(7) == '-'){
                                    try{
                                    Integer.parseInt(parameter.substring(0, 4));
                                    Integer.parseInt(parameter.substring(5, 7));
                                    Integer.parseInt(parameter.substring(8));
                                    sqlCommand += " '" + parameter + "',";
                                    }
                                    catch(NumberFormatException e){
                                        throw new Exception("There are non-numeric characters in Start Date. Abandoning save operation");
                                    }
                                }
                                else
                                  throw new Exception("Start Date is incorrectly formatted. Abandoning save operation.");  
                            }
                            parameter = endDateTextArea.getText();
                            if(parameter.equals(""))
                                sqlCommand += " " + null + ",";
                            else if(parameter.length() != 10)
                                throw new Exception("End Date has an incorrect number of characters. Abandoning save operation");
                            else{
                                if(parameter.charAt(4) == '-' && parameter.charAt(7) == '-'){
                                    try{
                                    Integer.parseInt(parameter.substring(0, 4));
                                    Integer.parseInt(parameter.substring(5, 7));
                                    Integer.parseInt(parameter.substring(8));
                                    sqlCommand += " '" + parameter + "',";
                                    }
                                    catch(NumberFormatException e){
                                        throw new Exception("There are non-numeric characters in End Date. Abandoning save operation");
                                    }
                                }
                                else
                                    throw new Exception("End Date is incorrectly formatted. Abandoning save operation.");
                            }
                            parameter = ratingTextArea.getText();
                            if(parameter.equals(""))
                                sqlCommand += " " + null + ", '";
                            else{
                                try{
                                    Double rating = Double.parseDouble(parameter);
                                    if(rating < 0.0 || rating > 10.0)
                                        throw new Exception("Rating is out of range. Abandoning save operation");
                                    else
                                        sqlCommand += " '" + parameter + "', '";
                                    
                                }
                                catch(NumberFormatException e){
                                    throw new Exception("Rating is not a number. Abandoning save operation");
                                }
                            }
                            sqlCommand += coverPath + "');";

                            Statement statement = connection.createStatement();
                            statement.execute(sqlCommand);
                            
                            refreshTable(tableArrList.get(currBookListIndex).getNameInDB());
                            secondaryStage.close();
                        }
                        catch (SQLException e) {
                            showError(e.getMessage());
                            e.printStackTrace();
                        }
                        catch (Exception e){
                            showError(e.getMessage());
                        }
                        
                    }
                });
                
                cancel.setOnAction(Event -> { 
                    secondaryStage.close();
                });

                secondaryStage.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
                    if (e.getCode() == KeyCode.ENTER) {
                        save.fire();
                    }
                });

                secondaryStage.show();
            }
    }

    /**
     * Remove a book from the currently selected book list
     * @param primaryStage
     */
    public void removeBook(Stage primaryStage){
        if(currBookListIndex == -1){
                showAlert("Please select a book list.");
            }
            else if(primaryTable.getItems().isEmpty()){
                showAlert("The current list is empty.");
            }
            else{
                Stage secondaryStage = new Stage();
                secondaryStage.initModality(Modality.WINDOW_MODAL);
                secondaryStage.initOwner(primaryStage);
                secondaryStage.getIcons().add(new Image("file:testingjavafx\\src\\main\\resources\\projectImages\\Bookcase.png"));
                secondaryStage.setWidth(260);
                secondaryStage.setTitle("Remove Book");
                secondaryStage.setResizable(false);
                String selectedBook[] = {"_NULL_"};
                MenuButton removeMenuButton = new MenuButton("-Select a Book-");
                try (Connection connection = DriverManager.getConnection(connectionUrl);) {
                    Statement statement = connection.createStatement();
                    ResultSet resultSet = statement.executeQuery("SELECT title FROM " + tableArrList.get(currBookListIndex).getNameInDB());
                    
                    while(resultSet.next()){
                        MenuItem removeMenuItem = new MenuItem(resultSet.getString(1));
                        removeMenuItem.setOnAction(even -> {
                            selectedBook[0] = removeMenuItem.getText();
                            removeMenuButton.setText(selectedBook[0]);
                        });
                        removeMenuButton.getItems().add(removeMenuItem);
                    }
                }
                catch (SQLException e) {
                    e.printStackTrace();
                }
                    
                Button save = new Button("Save");
                secondaryStage.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
                    if (e.getCode() == KeyCode.ENTER) {
                        save.fire();

                    }
                });
                save.setOnAction(Event -> { 
                    if(selectedBook[0].equals("_NULL_")){
                    showAlert("Please select a book to remove."); 
                    }
                    else{
                        try (Connection connection = DriverManager.getConnection(connectionUrl);) {
                        Statement statement = connection.createStatement();
                        ResultSet resultSet = statement.executeQuery("SELECT coverPath FROM " + tableArrList.get(currBookListIndex).getNameInDB() + " WHERE title='" + selectedBook[0] + "';");
                        if(resultSet.next()){
                            String path = resultSet.getString(1);
                            try{
                                Files.deleteIfExists(Paths.get(path));
                            }
                            catch(IOException e){
                                e.printStackTrace();
                            }
                        }
                        statement.execute("DELETE FROM " + tableArrList.get(currBookListIndex).getNameInDB() + " WHERE title='" + selectedBook[0] + "';");
                        refreshTable(tableArrList.get(currBookListIndex).getNameInDB());
                        secondaryStage.close();
                        }
                        catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                });
                Button cancel = new Button("Cancel");
                cancel.setOnAction(Event -> { 
                    secondaryStage.close();
                });

                HBox exitHBox = new HBox(save, cancel);
                exitHBox.setSpacing(5);
                VBox removeBookVBox = new VBox(removeMenuButton, exitHBox);
                removeBookVBox.setSpacing(10);
                removeBookVBox.setPadding(new Insets(5));
                Scene removeBookScene = new Scene(removeBookVBox);
                secondaryStage.setScene(removeBookScene);
                secondaryStage.setResizable(false);
                secondaryStage.show();
            }
    }

    /**
     * Move a book from one list to another
     * @param primaryStage
     */
    public void moveBook(Stage primaryStage){
        if(tableArrList.isEmpty()){
                showAlert("There are no existing tables.");
            }
            else if(tableArrList.size() == 1){
                showAlert("There is only one existing table.");
            }
            else{
                Stage secondaryStage = new Stage();
                secondaryStage.initModality(Modality.WINDOW_MODAL);
                secondaryStage.initOwner(primaryStage);
                secondaryStage.getIcons().add(new Image("file:testingjavafx\\src\\main\\resources\\projectImages\\Bookcase.png"));
                secondaryStage.setTitle("Move Book");
                secondaryStage.setResizable(false);
                
                MenuButton sourceMenuButton = new MenuButton("-Select a Book List-");
                MenuButton moveMenuButton = new MenuButton("-Select a Book-");
                MenuButton destinationMenuButton = new MenuButton("-Select a Book List-"); 

                String[] fromList = {"_NULL_"};
                String[] toList = {"_NULL_"};
                String[] selection = {"_NULL_"};
                
                for(int i = 0; i < tableArrList.size(); i++){
                    MenuItem sourceMenuItem = new MenuItem(tableArrList.get(i).getName());
                    sourceMenuItem.setOnAction(even-> {
                        fromList[0] = sourceMenuItem.getText();
                        sourceMenuButton.setText(fromList[0]);
                        moveMenuButton.getItems().clear();
                        try (Connection connection = DriverManager.getConnection(connectionUrl);) {
                            Statement statement = connection.createStatement();
                            ResultSet resultSet = statement.executeQuery("SELECT title FROM " + fromList[0].replace(" ", ""));
                            
                            while(resultSet.next()){
                                MenuItem bookOption = new MenuItem(resultSet.getString(1));
                                bookOption.setOnAction(eve -> {
                                    selection[0] = bookOption.getText();
                                    moveMenuButton.setText(selection[0]);
                                });
                                moveMenuButton.getItems().add(bookOption);
                            }
                        }
                        catch (SQLException e) {
                            e.printStackTrace();
                        }
                    });
                    sourceMenuButton.getItems().add(sourceMenuItem);
                    MenuItem destinationMenuItem = new MenuItem(tableArrList.get(i).getName());
                    destinationMenuItem.setOnAction(even-> {
                        toList[0] = destinationMenuItem.getText();
                        destinationMenuButton.setText(toList[0]);
                    });
                    destinationMenuButton.getItems().add(destinationMenuItem);
                    
                }

                Button save = new Button("Save");
                save.setOnAction(even -> {
                    if(selection[0].equals("_NULL_")){
                        showAlert("No book selected.");
                    }
                    else if(fromList[0].equals("_NULL_")){
                        showAlert("No source list selected.");
                    }
                    else if(toList[0].equals("_NULL")){
                        showAlert("No destination list selected.");
                    }
                    else if(fromList[0].equals(toList[0])){
                        showAlert("Source and destination lists cannot be the same.");
                    }
                    else{
                        try (Connection connection = DriverManager.getConnection(connectionUrl);) {
                            Statement statement = connection.createStatement();
                            ResultSet resultSet = statement.executeQuery("SELECT * FROM " + fromList[0].replace(" ", "") + " WHERE title='" + selection[0] + "';");
                            resultSet.next();

                            String startDate, endDate;
                            if(resultSet.getString(7) == null)
                                startDate = " " + null + ",";
                            else
                                startDate = " '" + resultSet.getString(7) + "',";
                            if(resultSet.getString(8) == null)
                                endDate = " " + null + ",";
                            else
                                endDate = " '" + resultSet.getString(8) + "',";
                            String sqlStatement = "INSERT INTO " + toList[0].replace(" ", "") + " (title, author, yearPublished, numPages, isbn, startDate, endDate, rating, coverPath) VALUES ('" +  resultSet.getString(2) + "', '" + resultSet.getString(3) + "', " + resultSet.getString(4) + ", " + resultSet.getString(5) + ", '" + resultSet.getString(6) + "'," + startDate + endDate + " " + resultSet.getString(9) + ", '" + resultSet.getString(10) + "');";
                            statement.execute(sqlStatement);
                            
                            statement.execute("DELETE FROM " + fromList[0].replace(" ", "") + " WHERE title='" + selection[0] + "';");
                            secondaryStage.close(); 
                            if(currBookList.equals(fromList[0]))
                                refreshTable(tableArrList.get(currBookListIndex).getNameInDB());
                            else if(currBookList.equals(toList[0]))
                                refreshTable(tableArrList.get(currBookListIndex).getNameInDB());
                        }
                        catch(SQLException e){
                            e.printStackTrace();
                        }
                    }
                });
                
                Button cancel = new Button ("Cancel");
                cancel.setOnAction(even -> {
                    secondaryStage.close();
                });

                secondaryStage.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
                    if (e.getCode() == KeyCode.ENTER) 
                        save.fire();
                });

                Label sourceLabel = new Label("Source");
                Label colon = new Label(":");
                colon.setFont(new Font(15));
                HBox sourceHBox = new HBox(sourceMenuButton, colon, moveMenuButton);
                sourceHBox.setSpacing(5);
                VBox sourceVBox = new VBox(sourceLabel, sourceHBox);
                Label destinationLabel = new Label("Destination");
                VBox destinationVBox = new VBox(destinationLabel, destinationMenuButton);
                HBox exitHBox = new HBox(save, cancel);
                exitHBox.setSpacing(5);
                VBox moveVBox = new VBox(sourceVBox, destinationVBox, exitHBox);
                moveVBox.setSpacing(10);
                moveVBox.setPadding(new Insets(5));
                Scene moveScene = new Scene(moveVBox);
                secondaryStage.setScene(moveScene);
                secondaryStage.show();
            }
    }

    /**
     * Edit a book in the currently viewed book list
     * @param primaryStage
     */
    public void editBook(Stage primaryStage){
        if(currBookListIndex == -1){
                showAlert("Please select a book list.");
            }
            else if(primaryTable.getItems().isEmpty()){
                showAlert("The current book list is empty.");
            }
            else{
                Stage secondaryStage = new Stage();
                secondaryStage.initModality(Modality.WINDOW_MODAL);
                secondaryStage.initOwner(primaryStage);
                secondaryStage.setWidth(500);
                secondaryStage.setHeight(720);
                secondaryStage.setTitle("Edit Book");
                secondaryStage.getIcons().add(new Image("file:testingjavafx\\src\\main\\resources\\projectImages\\Bookcase.png"));
                secondaryStage.setResizable(false);
                String[] selection = {"_NULL"};
                File selectedFile[] = {null};

                Label titleLabel = new Label("Title");
                TextArea titleTextArea = new TextArea();
                Label authorLabel = new Label("Author");
                TextArea authorTextArea = new TextArea();
                Label publishDateLabel = new Label("Year Published");
                TextArea publishTextArea = new TextArea();
                Label numPagesLabel = new Label("Number of Pages");
                TextArea numPagesTextArea = new TextArea();
                Label isbnLabel = new Label("ISBN-13");
                TextArea isbnTextArea = new TextArea();
                Label startDateLabel = new Label("Started Reading (YYYY-MM-DD)");
                TextArea startDateTextArea = new TextArea();
                Label endDateLabel = new Label("Finished Reading (YYYY-MM-DD)");
                TextArea endDateTextArea = new TextArea();
                Label ratingLabel = new Label("Rating (0.0 - 10.0)");
                TextArea ratingTextArea = new TextArea();
                Label coverLabel = new Label("Cover Art");

                titleTextArea.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
                    if (e.getCode() == KeyCode.ENTER) 
                        e.consume();
                    
                    if (e.getCode() == KeyCode.DOWN) 
                        authorTextArea.requestFocus();
            
                });

                titleTextArea.setMaxHeight(15);
                authorTextArea.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
                    if (e.getCode() == KeyCode.ENTER) 
                        e.consume();
                    
                    if (e.getCode() == KeyCode.DOWN)
                        publishTextArea.requestFocus();
                    
                    if (e.getCode() == KeyCode.UP)
                        titleTextArea.requestFocus();
                    
                });
                authorTextArea.setMaxHeight(15);
                publishTextArea.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
                    if (e.getCode() == KeyCode.ENTER) 
                        e.consume();
                    
                    if (e.getCode() == KeyCode.DOWN) 
                        numPagesTextArea.requestFocus();
                    
                    if (e.getCode() == KeyCode.UP) 
                        authorTextArea.requestFocus();
                    
                });
                publishTextArea.setMaxHeight(15);
                numPagesTextArea.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
                    if (e.getCode() == KeyCode.ENTER) 
                        e.consume();
                    
                    if (e.getCode() == KeyCode.DOWN) 
                        isbnTextArea.requestFocus();
                    
                    if (e.getCode() == KeyCode.UP) 
                        publishTextArea.requestFocus();
                    
                });
                numPagesTextArea.setMaxHeight(15);
                isbnTextArea.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
                    if (e.getCode() == KeyCode.ENTER) 
                        e.consume();
                    
                    if (e.getCode() == KeyCode.DOWN) 
                        startDateTextArea.requestFocus();
                    
                    if (e.getCode() == KeyCode.UP) 
                        numPagesTextArea.requestFocus();
                    
                });
                isbnTextArea.setMaxHeight(15);
                startDateTextArea.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
                    if (e.getCode() == KeyCode.ENTER) 
                        e.consume();
                    
                    if (e.getCode() == KeyCode.DOWN) 
                        endDateTextArea.requestFocus();
                    
                    if (e.getCode() == KeyCode.UP) 
                        isbnTextArea.requestFocus();
                    
                });
                startDateTextArea.setMaxHeight(15);
                endDateTextArea.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
                    if (e.getCode() == KeyCode.ENTER) 
                        e.consume();
                    
                    if (e.getCode() == KeyCode.DOWN) 
                        ratingTextArea.requestFocus();
                    
                    if (e.getCode() == KeyCode.UP)
                        startDateTextArea.requestFocus(); 
                    
                });
                endDateTextArea.setMaxHeight(15);
                ratingTextArea.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
                    if (e.getCode() == KeyCode.ENTER) 
                        e.consume();
                    
                    if (e.getCode() == KeyCode.UP) 
                        endDateTextArea.requestFocus(); 
                    
                });
                ratingTextArea.setMaxHeight(15);

                FileChooser fileChooser = new FileChooser();
                FileChooser.ExtensionFilter imageFilter = 
                    new FileChooser.ExtensionFilter("Image Files (*.png, *.jpg, *jpeg)", "*.png", "*.jpg", "*.jpeg");
                fileChooser.getExtensionFilters().add(imageFilter);

                Button selectFileButton = new Button("Select Image");
                Button removeFileButton = new Button("Remove Image");
                Label selectedFileLabel = new Label();
                

                MenuButton editMenuButton = new MenuButton("-Select a Book-");
                try (Connection connection = DriverManager.getConnection(connectionUrl);) {
                    Statement statement = connection.createStatement();
                    
                    ResultSet resultSet = statement.executeQuery("SELECT title FROM " + tableArrList.get(currBookListIndex).getNameInDB());
                    while(resultSet.next()){
                        MenuItem editMenuItem = new MenuItem(resultSet.getString(1));
                        editMenuItem.setOnAction(even -> {
                            selection[0] = editMenuItem.getText();
                            editMenuButton.setText(selection[0]);
                            try(Connection connection1 = DriverManager.getConnection(connectionUrl);){
                                Statement statement1 = connection1.createStatement();
                                ResultSet resultSet1 = statement1.executeQuery("SELECT * FROM " + tableArrList.get(currBookListIndex).getNameInDB() + " WHERE title='" + selection[0] + "';");
                                resultSet1.next();
                                titleTextArea.setText(resultSet1.getString(2));
                                authorTextArea.setText(resultSet1.getString(3));
                                publishTextArea.setText(resultSet1.getString(4));
                                numPagesTextArea.setText(resultSet1.getString(5));
                                isbnTextArea.setText(resultSet1.getString(6));
                                startDateTextArea.setText(resultSet1.getString(7));
                                endDateTextArea.setText(resultSet1.getString(8));
                                ratingTextArea.setText(resultSet1.getString(9));
                                if(resultSet1.getString(10).equals("null")){
                                    selectedFileLabel.setText("");
                                }
                                else if(!resultSet1.getString(10).equals("")){
                                    String str = resultSet1.getString(10);
                                    selectedFile[0] = new File(str);//Files.//Files.get(resultSet1.getString(6));
                                    selectedFileLabel.setText(str.substring(str.lastIndexOf("\\") + 1));
                                }
                            }
                            catch(SQLException e){
                                e.printStackTrace();
                            }
                        });
                        editMenuButton.getItems().add(editMenuItem);
                    }
                }
                catch(SQLException e){
                    e.printStackTrace();
                }

                selectFileButton.setOnAction(Event -> {
                    if(editMenuButton.getText().equals("-Select a Book-"))
                        showAlert("Please select a Book");
                    else{
                        selectedFile[0] = fileChooser.showOpenDialog(secondaryStage);
                        if(selectedFile[0] != null){
                            selectedFileLabel.setText(selectedFile[0].getName());
                        }

                    }
                });

                removeFileButton.setOnAction(even -> {
                    if(editMenuButton.getText().equals("-Select a Book-"))
                        showAlert("Please select a book");
                    else{
                    selectedFile[0] = null;
                    selectedFileLabel.setText("");
                    }
                });

                Button save = new Button("Save");
                save.setOnAction(even -> {
                    if(editMenuButton.getText().equals("-Select a Book-")){
                        showAlert("Please select a book.");
                    }
                    else if(titleTextArea.getText().equals("")){
                        showAlert("Every book requires a title.");
                    }
                    else{
                        
                        String coverPath = null;
    
                        if(selectedFile[0] != null && selectedFile[0].exists()){
                            coverPath = selectedFile[0].getAbsolutePath();
                            Path source = Paths.get(coverPath); 
                            String publishText;
                            if(publishTextArea.getText() == null)
                                publishText = "";
                            else
                                publishText = publishTextArea.getText();

                            Path destination = Paths.get("testingjavafx\\src\\main\\resources\\coverImages\\" + titleTextArea.getText() + "-" + authorTextArea.getText() + selectedFile[0].toString().substring(selectedFile[0].toString().lastIndexOf('.')));
                            File destinationFile = destination.toFile();

                            if(destinationFile.exists()){ 
                                        String destinationPathStr = destination.toString();
                                        int fileTypeIndex = destinationFile.toString().lastIndexOf('.');
                                        int copyNum = 0;
                                        boolean foundAvailableName = true;
                                        while(foundAvailableName){
                                            copyNum++;
                                            destination = Paths.get(destinationPathStr.substring(0, fileTypeIndex) + " (" + copyNum + ")" + destinationPathStr.substring(fileTypeIndex));
                                            destinationFile = destination.toFile();
                                            if(!destinationFile.exists()){
                                                foundAvailableName = false;
                                            }
                                        }

                                        try{
                                            coverPath = (Files.copy(source, destination)).toString();

                                        }
                                        catch(IOException e){
                                            e.printStackTrace();
                                        }
                            }
                            else{
                                try{
                                    coverPath = (Files.copy(source, destination)).toString();
                                }
                                catch(IOException e){
                                    e.printStackTrace();
                                    
                                }
                            }
                        }
                        try (Connection connection = DriverManager.getConnection(connectionUrl);) {
                            Statement statement = connection.createStatement();
                            if(selectedFile[0] == null){
                                ResultSet resultSet = statement.executeQuery("SELECT coverPath FROM " + tableArrList.get(currBookListIndex).getNameInDB() + " WHERE title='" + titleTextArea.getText() + "';");
                                if(resultSet.next()){
                                        Files.delete(Paths.get(resultSet.getString(1)));
                                }
                            }
                                
                            String sqlCommand = "UPDATE " + tableArrList.get(currBookListIndex).getNameInDB() + " SET title='";
                                    
                                    String parameter = titleTextArea.getText();
                                    if(parameter.length() > 100)
                                        throw new Exception("Title is greater than 100 characters. Abandoning save operation.");
                                    else
                                        sqlCommand += parameter + "', author='";

                                    parameter = authorTextArea.getText();
                                    if(parameter.length() > 50)
                                        throw new Exception("Author is greater than 50 characters. Abandoning save operation.");
                                    else    
                                        sqlCommand += parameter + "',";

                                    parameter = publishTextArea.getText();
                                    if(parameter == null){
                                        sqlCommand += " yearPublished=" + null + ",";
                                    }
                                    else if(parameter.equals(""))
                                        sqlCommand += " yearPublished=" + null + ",";
                                    else{
                                        try{
                                            int numParam = Integer.parseInt(parameter);
                                            sqlCommand += " yearPublished='" + numParam + "',";
                                        }
                                        catch(NumberFormatException e){
                                            throw new Exception("A non-numerical charcter was entered in the Year Published text area. Abandoning save operation.");
                                        }
                                    }
                                    parameter = numPagesTextArea.getText();
                                    if(parameter == null)
                                        sqlCommand += " numPages=" + null + ", isbn='";
                                    else if(parameter.equals(""))
                                        sqlCommand += " numPages=" + null + ", isbn='";
                                    else{
                                        try{
                                            int numParam = Integer.parseInt(parameter);
                                            sqlCommand += " numPages='" + numParam + "', isbn='";
                                        }
                                        catch(NumberFormatException e){
                                            throw new Exception("A non-numerical charcter was entered in the Number of Pages text area. Abandoning save operation.");
                                        }
                                    }
                                    parameter = isbnTextArea.getText();
                                    if(parameter.equals(""))
                                        sqlCommand += parameter + "',";
                                    else if(parameter.length() > 17)
                                        throw new Exception("ISBN-13 has an incorrect number of characters. Abandoning save operation");
                                    else{
                                        sqlCommand += parameter + "',";
                                    }
                                    parameter = startDateTextArea.getText();
                                    if(parameter == null)
                                        sqlCommand += " startDate=" + null + ",";
                                    else if(parameter.equals(""))
                                        sqlCommand += " startDate=" + null + ",";
                                    else if(parameter.length() != 10)
                                        throw new Exception("Start Date has an incorrect number of characters. Abandoning save operation");     
                                    else{
                                        if(parameter.charAt(4) == '-' && parameter.charAt(7) == '-'){
                                            try{
                                            Integer.parseInt(parameter.substring(0, 4));
                                            Integer.parseInt(parameter.substring(5, 7));
                                            Integer.parseInt(parameter.substring(8));
                                            sqlCommand += " startDate='" + parameter + "',";
                                            }
                                            catch(NumberFormatException e){
                                                throw new Exception("There are non-numeric characters in Start Date. Abandoning save operation");
                                            }
                                        }
                                        else
                                        throw new Exception("Start Date is incorrectly formatted. Abandoning save operation.");  
                                    }
                                    parameter = endDateTextArea.getText();
                                    if(parameter == null)
                                        sqlCommand += " endDate=" + null + ",";
                                    else if(parameter.equals(""))
                                        sqlCommand += " endDate=" + null + ",";
                                    else if(parameter.length() != 10)
                                        throw new Exception("End Date has an incorrect number of characters. Abandoning save operation");
                                    else{
                                        if(parameter.charAt(4) == '-' && parameter.charAt(7) == '-'){
                                            try{
                                            Integer.parseInt(parameter.substring(0, 4));
                                            Integer.parseInt(parameter.substring(5, 7));
                                            Integer.parseInt(parameter.substring(8));
                                            sqlCommand += " endDate='" + parameter + "',";
                                            }
                                            catch(NumberFormatException e){
                                                throw new Exception("There are non-numeric characters in End Date. Abandoning save operation");
                                            }
                                        }
                                        else
                                            throw new Exception("End Date is incorrectly formatted. Abandoning save operation.");
                                    }
                                    parameter = ratingTextArea.getText();
                                    if(parameter == null)
                                        sqlCommand += " rating=" + null + ",";
                                    else if(parameter.equals(""))
                                        sqlCommand += " rating=" + null + ",";
                                    else{
                                        try{
                                            Double rating = Double.parseDouble(parameter);
                                            if(rating < 0.0 || rating > 10.0)
                                                throw new Exception("Rating is out of range. Abandoning save operation");
                                            else
                                                sqlCommand += " rating='" + parameter + "',";
                                            
                                        }
                                        catch(NumberFormatException e){
                                            throw new Exception("Rating is not a number. Abandoning save operation");
                                        }
                                    }

                                    if(coverPath == null)
                                        sqlCommand += " coverPath=" + null + " WHERE title='" + selection[0] + "';";
                                    else if(coverPath.equals(""))
                                        sqlCommand += " coverPath=" + null + " WHERE title='" + selection[0] + "';";
                                    else
                                        sqlCommand += " coverPath='" + coverPath + "' WHERE title='" + selection[0] + "';";
   

                            statement.execute(sqlCommand);
                            refreshTable(tableArrList.get(currBookListIndex).getNameInDB());     
                            secondaryStage.close();
                        }
                        catch (SQLException e) {
                            showError(e.getMessage());
                            e.printStackTrace();
                        }

                        catch (Exception e){
                            showError(e.getMessage());
                            e.printStackTrace();
                        }
                    }

                
                });

                Button cancel = new Button("Cancel");
                cancel.setOnAction(even -> {
                    secondaryStage.close();
                });

                secondaryStage.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
                    if (e.getCode() == KeyCode.ENTER) 
                        save.fire();
                });

                HBox fileHBox = new HBox(selectFileButton, selectedFileLabel);
                VBox titleVBox = new VBox(titleLabel, titleTextArea);
                VBox authorVBox = new VBox(authorLabel, authorTextArea);
                VBox publishVBox = new VBox(publishDateLabel, publishTextArea);
                VBox numPagesVBox = new VBox(numPagesLabel, numPagesTextArea);
                VBox isbnVBox = new VBox(isbnLabel, isbnTextArea);
                VBox startDateVBox = new VBox(startDateLabel, startDateTextArea);
                VBox endDateVBox = new VBox(endDateLabel, endDateTextArea);
                VBox ratingVBox = new VBox(ratingLabel, ratingTextArea);
                VBox fileButtonVBox =  new VBox(fileHBox, removeFileButton);
                fileButtonVBox.setSpacing(5);
                VBox fileVBox = new VBox(coverLabel, fileButtonVBox);
                HBox exitHBox = new HBox(save, cancel);
                exitHBox.setSpacing(5);
                VBox editVBox = new VBox(editMenuButton, titleVBox, authorVBox, publishVBox, numPagesVBox,
                    isbnVBox, startDateVBox, endDateVBox, ratingVBox, fileVBox, exitHBox);
                editVBox.setSpacing(10);
                editVBox.setPadding(new Insets(5));
                Scene editScene = new Scene(editVBox);
                secondaryStage.setScene(editScene);
                secondaryStage.show();
            }
    }

    /**
     * clear the currently viewed book list
     * @param primaryStage
     */
    public void clearTable(Stage primaryStage){
        if(currBookListIndex == -1){
                showAlert("Select a book list.");
            }
            else if(primaryTable.getItems().isEmpty()){
                showAlert("The current book list is empty.");
            }
            else{
                Stage secondaryStage = new Stage();
                secondaryStage.setTitle("Clear List");
                secondaryStage.initModality(Modality.WINDOW_MODAL);
                secondaryStage.initOwner(primaryStage);
                secondaryStage.getIcons().add(new Image("file:testingjavafx\\src\\main\\resources\\projectImages\\Bookcase.png"));
                secondaryStage.setResizable(false);

                Alert alert = new Alert(AlertType.CONFIRMATION);
                alert.setContentText("Are you sure you want to the book list \"" + currBookList + "\"?");
                Optional<ButtonType> b = alert.showAndWait();
                if(b.get() == ButtonType.OK){
                    try (Connection connection = DriverManager.getConnection(connectionUrl);) {
                        Statement statement = connection.createStatement();
                        ResultSet resultSet = statement.executeQuery("SELECT coverPath FROM " + tableArrList.get(currBookListIndex).getNameInDB());
                        while(resultSet.next()){
                            try{
                                if(Files.exists(Paths.get(resultSet.getString(1))))
                                    Files.delete(Paths.get(resultSet.getString(1)));
                            }
                            catch(IOException e){
                                e.printStackTrace();
                            }
                        }
                        statement.execute("DELETE FROM " + tableArrList.get(currBookListIndex).getNameInDB());
                    }
                    catch(SQLException e){
                        e.printStackTrace();
                    }
                    refreshTable(tableArrList.get(currBookListIndex).getNameInDB());
                }
            }
    }

    /**
     * Create a new table in the database that reprsents a new book list
     * @param primaryStage
     */
    public void createTable(Stage primaryStage){
        Stage secondaryStage = new Stage();
            secondaryStage.setTitle("New Table");
            secondaryStage.initModality(Modality.WINDOW_MODAL);
            secondaryStage.initOwner(primaryStage);
            secondaryStage.setResizable(false);

            Label name = new Label("Table Name");
            TextArea nameTextArea = new TextArea("");
            Button save = new Button("Save");
            Button cancel = new Button("Cancel");
            nameTextArea.setMaxHeight(15);
            secondaryStage.getIcons().add(new Image("file:testingjavafx\\src\\main\\resources\\projectImages\\Bookcase.png"));

            

            save.setOnAction(even -> {
                if(nameTextArea.getText().equals(""))
                    showAlert("The table title is empty");
                else{
                    try(Connection connection = DriverManager.getConnection(connectionUrl);){
                        Statement statement = connection.createStatement();
                        String changedName = nameTextArea.getText().replace(" ","");
                        statement.execute("CREATE TABLE " + changedName +  " (id INT PRIMARY KEY IDENTITY(1,1), title varchar(100), author varchar(50), yearPublished SMALLINT, numPages SMALLINT, isbn varchar(17), startDate DATE, endDate DATE, rating REAL, coverPath varchar(200))");
                        tableArrList.add(new Table(nameTextArea.getText()));
                        statement.execute("INSERT INTO _SETTINGS_ (tableName, tableNameDB, author, yearPublished, numPages, isbn, startDate, endDate, rating, cover) VALUES ('" + nameTextArea.getText() + "', '" + changedName + "', 1, 1, 1, 1, 1, 1, 1, 1);");
                        refreshPrimaryMenuButton();
                        secondaryStage.close();
                    }
                    catch(SQLException e){
                        e.printStackTrace();
                    }
                }
            });

            nameTextArea.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
                    if (e.getCode() == KeyCode.ENTER) 
                        e.consume();
            });

            secondaryStage.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
                    if (e.getCode() == KeyCode.ENTER) {
                        save.fire();
                    }
                });

            cancel.setOnAction(even -> {
                secondaryStage.close();
            });

            HBox exitHBox = new HBox(save, cancel);
            exitHBox.setSpacing(5);
            VBox nameVBox = new VBox(name, nameTextArea);
            VBox stageVBox = new VBox(nameVBox, exitHBox);
            stageVBox.setSpacing(10);
            stageVBox.setPadding(new Insets(5));
            Scene newTableScene = new Scene(stageVBox);
            secondaryStage.setScene(newTableScene);
            secondaryStage.show();
    }

    public void deleteTable(Stage primaryStage){
        if(tableArrList.isEmpty()){
                showAlert("There are no existing book lists.");
            }
            else{
                Stage secondaryStage = new Stage();

                secondaryStage.setTitle("Delete Table");
                secondaryStage.getIcons().add(new Image("file:testingjavafx\\src\\main\\resources\\projectImages\\Bookcase.png"));
                secondaryStage.initModality(Modality.WINDOW_MODAL);
                secondaryStage.initOwner(primaryStage);
                secondaryStage.setWidth(260);
                secondaryStage.setResizable(false);

                MenuButton tableMenuButton = new MenuButton("-Select a Table-");
                Button save = new Button("Save");
                Button cancel = new Button("Cancel");
                String[] selectedName = new String[1];

                for(int i = 0; i < tableArrList.size(); i++){
                    MenuItem menuItem = new MenuItem(tableArrList.get(i).getName());
                    menuItem.setOnAction(even -> {
                        selectedName[0] = menuItem.getText();
                        tableMenuButton.setText(menuItem.getText());
                    });
                    tableMenuButton.getItems().add(menuItem);
                }

                save.setOnAction(even -> {
                    try(Connection connection = DriverManager.getConnection(connectionUrl);){
                        Statement statement = connection.createStatement();
                        
                        for(int i = 0; i < tableArrList.size(); i++){
                            if(selectedName[0].equals(tableArrList.get(i).getName())){
                                statement.execute("DROP TABLE " + tableArrList.get(i).getNameInDB());
                                statement.execute("DELETE FROM _SETTINGS_ WHERE tableName='" + tableArrList.get(i).getName() + "';");
                                if(i == currBookListIndex){
                                    currBookListIndex = -1;
                                    Label placeHolderLabel = new Label("Select a Book List");
                                    placeHolderLabel.setFont(new Font(40));
                                    primaryTable.setPlaceholder(placeHolderLabel);
                                    primaryTable.getItems().clear();
                                    updateCurrBookLists("-Select a Book List-");
                                }
                                tableArrList.remove(i);
                                break;
                            }
                                
                        }
                        refreshPrimaryMenuButton();
                        secondaryStage.close();
                    }
                    catch(SQLException e){
                        e.printStackTrace();
                    }
                    
                });

                secondaryStage.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
                    if (e.getCode() == KeyCode.ENTER) {
                        save.fire();
                    }
                });

                cancel.setOnAction(even -> {
                    secondaryStage.close();
                });

                HBox exitHBox = new HBox(save, cancel);
                exitHBox.setSpacing(5);
                VBox deleteTableVBox = new VBox(tableMenuButton, exitHBox);
                deleteTableVBox.setSpacing(10);
                deleteTableVBox.setPadding(new Insets(5));
                Scene deleteTableVBoxScene = new Scene(deleteTableVBox);
                secondaryStage.setScene(deleteTableVBoxScene);
                secondaryStage.show();
            }
    }

    public void editShownColumns(Stage primaryStage){
        if(tableArrList.isEmpty()){
                showAlert("There are no existing book lists.");
            }
            else{
                Stage secondaryStage = new Stage();
                secondaryStage.setTitle("Edit Shown Columns");
                secondaryStage.initModality(Modality.WINDOW_MODAL);
                secondaryStage.initOwner(primaryStage);
                secondaryStage.getIcons().add(new Image("file:testingjavafx\\src\\main\\resources\\projectImages\\Bookcase.png"));
                secondaryStage.setResizable(false);

                CheckBox authorCheck = new CheckBox("Author");
                CheckBox yearPublishedCheck = new CheckBox("Year Published");
                CheckBox isbnCheck = new CheckBox("ISBN-13");
                CheckBox numPagesCheck = new CheckBox("Page Number");
                CheckBox startDateCheck = new CheckBox("Start Date");
                CheckBox endDateCheck = new CheckBox("End Date");
                CheckBox ratingCheck = new CheckBox("Rating");
                CheckBox imageCheck = new CheckBox("Cover Art");
                Button applyToCurrList = new Button("Apply to Current List");
                Button applyToAllLists = new Button("Apply to All Lists");
                Button cancel = new Button("Cancel");

                if(currBookListIndex == -1){
                    authorCheck.setSelected(true);
                    yearPublishedCheck.setSelected(true);
                    isbnCheck.setSelected(true);
                    numPagesCheck.setSelected(true);
                    startDateCheck.setSelected(true);
                    endDateCheck.setSelected(true);
                    ratingCheck.setSelected(true);
                    imageCheck.setSelected(true);
                }
                else{
                    try(Connection connection = DriverManager.getConnection(connectionUrl);){
                        Statement statement = connection.createStatement();
                        ResultSet resultSet = statement.executeQuery("SELECT * FROM _SETTINGS_ WHERE tableName='" + tableArrList.get(currBookListIndex).getName() + "';");
                        resultSet.next();
                        authorCheck.setSelected(BITToBool(resultSet.getString(4)));
                        yearPublishedCheck.setSelected(BITToBool(resultSet.getString(5)));
                        isbnCheck.setSelected(BITToBool(resultSet.getString(6)));
                        numPagesCheck.setSelected(BITToBool(resultSet.getString(7)));
                        startDateCheck.setSelected(BITToBool(resultSet.getString(8)));
                        endDateCheck.setSelected(BITToBool(resultSet.getString(9)));
                        ratingCheck.setSelected(BITToBool(resultSet.getString(10)));
                        imageCheck.setSelected(BITToBool(resultSet.getString(11)));
                    }
                    catch(SQLException e){
                        e.printStackTrace();
                    }
                }

                applyToCurrList.setOnAction(even -> {
                    if(currBookListIndex == -1){
                        showAlert("Please select a book list.");
                    }
                    else{
                        try(Connection connection = DriverManager.getConnection(connectionUrl);){
                            Statement statement = connection.createStatement();
                            statement.execute("UPDATE _SETTINGS_ SET author=" + boolToBit((authorCheck.isSelected())) + ", yearPublished=" + boolToBit(yearPublishedCheck.isSelected())
                                + ", numPages=" + boolToBit(numPagesCheck.isSelected()) + ", isbn=" + boolToBit(isbnCheck.isSelected()) + ", startDate=" + boolToBit(startDateCheck.isSelected()) 
                                + ", endDate=" + boolToBit(endDateCheck.isSelected()) + ", rating=" + boolToBit(ratingCheck.isSelected()) + ", cover=" + boolToBit(imageCheck.isSelected()) 
                                + " WHERE tableName='" + tableArrList.get(currBookListIndex).getName() + "';");
                            secondaryStage.close();
                            refreshTable(tableArrList.get(currBookListIndex).getNameInDB());
                        }
                        catch(SQLException e){
                            e.printStackTrace();
                        }
                    }
                });

                applyToAllLists.setOnAction(even -> {
                     try(Connection connection = DriverManager.getConnection(connectionUrl);){
                        Statement statement = connection.createStatement();
                        for(int i = 0; i < tableArrList.size(); i++){
                            statement.execute("UPDATE _SETTINGS_ SET author=" + boolToBit((authorCheck.isSelected())) + ", yearPublished=" + boolToBit(yearPublishedCheck.isSelected())
                                + ", numPages=" + boolToBit(numPagesCheck.isSelected()) + ", isbn=" + boolToBit(isbnCheck.isSelected()) + ", startDate=" + boolToBit(startDateCheck.isSelected()) 
                                + ", endDate=" + boolToBit(endDateCheck.isSelected()) + ", rating=" + boolToBit(ratingCheck.isSelected()) + ", cover=" + boolToBit(imageCheck.isSelected()) 
                                + " WHERE tableName='" + tableArrList.get(i).getName() + "';");
                        }
                       
                        secondaryStage.close();
                        refreshTable(tableArrList.get(currBookListIndex).getNameInDB());
                    }
                    catch(SQLException e){
                        e.printStackTrace();
                    }

                });

                cancel.setOnAction(even -> {
                    secondaryStage.close();
                });

                HBox exitHBox = new HBox(applyToCurrList, applyToAllLists, cancel);
                exitHBox.setSpacing(5);
                VBox checkVBox = new VBox(authorCheck, yearPublishedCheck, numPagesCheck, isbnCheck, 
                    startDateCheck, endDateCheck, ratingCheck, imageCheck);
                checkVBox.setSpacing(5);
                VBox sceneVBox = new VBox(checkVBox, exitHBox);
                sceneVBox.setSpacing(10);
                sceneVBox.setPadding(new Insets(5));
                Scene showColumnScene = new Scene(sceneVBox);
                secondaryStage.setScene(showColumnScene);
                secondaryStage.show();

            }
    }
    
    
    
    
    @Override
    public void start(Stage primaryStage) throws Exception {

        RandomAccessFile lockFile = new RandomAccessFile("TheBookArchive\\src\\main\\resources\\lockFile.txt", "rw");
        FileChannel channel = lockFile.getChannel();
        lock = channel.tryLock();
        if(lock == null){
            System.exit(0);
        }


        try(Connection connection = DriverManager.getConnection(connectionUrl);){
            Statement statementName = connection.createStatement();
            Statement statementSettings = connection.createStatement();
            ResultSet resultSetName = statementName.executeQuery("SELECT name FROM SYS.TABLES");
            resultSetName.next(); //skip the settings table
            while(resultSetName.next()){
                ResultSet resultSetSettings = statementSettings.executeQuery("SELECT * FROM _SETTINGS_ WHERE tableNameDB='" + resultSetName.getString(1) + "';");
                resultSetSettings.next();
                tableArrList.add(new Table(resultSetSettings.getString(2)));
            }
        }
        catch(SQLException e){
            e.printStackTrace();
        }

        primaryStage.getIcons().add(new Image("file:testingjavafx\\src\\main\\resources\\projectImages\\Bookcase.png"));

        Button addButton = new Button("Add Book");
        addButton.setFont(new Font(15));

        Button removeButton = new Button("Remove Book");
        removeButton.setFont(new Font(15));

        Button moveButton = new Button("Move Book");
        moveButton.setFont(new Font(15));

        Button editButton = new Button("Edit Book");
        editButton.setFont(new Font(15));

        Button clearButton = new Button("Clear List");
        clearButton.setFont(new Font(15));

        Button exitProgramButton = new Button("Exit");
        exitProgramButton.setFont(new Font(15));

        Button newTable = new Button("New List");
        newTable.setFont(new Font(15));

        Button deleteTable = new Button("Delete List");
        deleteTable.setFont(new Font(15));

        Button editShownColumns = new Button("Edit Shown Columns");
        editShownColumns.setFont(new Font(15));

        refreshPrimaryMenuButton();
        Label placeHolderLabel = new Label("Select a Book List");
        placeHolderLabel.setFont(new Font(30));
        primaryTable.setPlaceholder(placeHolderLabel);
        
        TableColumn<Book, String> titleColumn = new TableColumn<>("Title");
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        titleColumn.setCellFactory(column -> {
            return new TableCell<Book, String>() {
                @Override 
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(item);
                    setAlignment(Pos.CENTER);

                }
            };
        });
        
        TableColumn<Book, String> authorColumn = new TableColumn<>("Author");
        authorColumn.setCellValueFactory(new PropertyValueFactory<>("author"));
        authorColumn.setCellFactory(column -> {
            return new TableCell<Book, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(item);
                    setAlignment(Pos.CENTER);

                }
            };
        });
        TableColumn<Book, String> dateColumn = new TableColumn<>("Year Published");
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("yearPublished"));
        dateColumn.setCellFactory(column -> {
            return new TableCell<Book, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(item);
                    setAlignment(Pos.CENTER);
                }
            };
        });
        TableColumn<Book, String> numPagesColumn = new TableColumn<>("Page Count");
        numPagesColumn.setCellValueFactory(new PropertyValueFactory<>("numPages"));
        numPagesColumn.setCellFactory(column -> {
            return new TableCell<Book, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(item);
                    setAlignment(Pos.CENTER);
                }
            };
        });
        TableColumn<Book, String> isbnColumn = new TableColumn<>("ISBN-13");
        isbnColumn.setCellValueFactory(new PropertyValueFactory<>("isbn"));
        isbnColumn.setCellFactory(column -> {
            return new TableCell<Book, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(item);
                    setAlignment(Pos.CENTER);
                }
            };
        });
        TableColumn<Book, String> startDateColumn = new TableColumn<>("Started Reading");
        startDateColumn.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        startDateColumn.setCellFactory(column -> {
            return new TableCell<Book, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(item);
                    setAlignment(Pos.CENTER);
                }
            };
        });
        TableColumn<Book, String> endDateColumn = new TableColumn<>("Finished Reading");
        endDateColumn.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        endDateColumn.setCellFactory(column -> {
            return new TableCell<Book, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(item);
                    setAlignment(Pos.CENTER);
                }
            };
        });
        TableColumn<Book, String> ratingColumn = new TableColumn<>("Rating");
        ratingColumn.setCellValueFactory(new PropertyValueFactory<>("rating"));
        ratingColumn.setCellFactory(column -> {
            return new TableCell<Book, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(item);
                    setAlignment(Pos.CENTER); 
                }
            };
        });
        TableColumn<Book, String> coverColumn = new TableColumn<>("Cover");
        coverColumn.setCellValueFactory(new PropertyValueFactory<>("imagePath"));
        coverColumn.setCellFactory(col -> new TableCell<>() {
            ImageView iv = new ImageView();
            @Override protected void updateItem(String path, boolean empty) {
                    iv.setImage(new Image("file:" + path, 160, 160, true, true));
                    setGraphic(iv);
                    setAlignment(Pos.CENTER);
                //}
            }
        });
        
        primaryTable.getColumns().add(titleColumn);
        primaryTable.getColumns().add(authorColumn);
        primaryTable.getColumns().add(dateColumn);
        primaryTable.getColumns().add(numPagesColumn);
        primaryTable.getColumns().add(isbnColumn);
        primaryTable.getColumns().add(startDateColumn);
        primaryTable.getColumns().add(endDateColumn);
        primaryTable.getColumns().add(ratingColumn);
        primaryTable.getColumns().add(coverColumn);

        primaryTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        primaryMenuButton.setFont(new Font("Yu Gothic", 20));

        //add Book
        addButton.setOnAction(event -> {
            addBook(primaryStage);
        });

        //remove Book
        removeButton.setOnAction(event -> {
            removeBook(primaryStage);
        });
        
        
        //Move Book
        moveButton.setOnAction(event -> {
            moveBook(primaryStage);

        });

        //Edit book
        editButton.setOnAction(event -> {
            editBook(primaryStage);
        });

        //Clear table
        clearButton.setOnAction(event -> {
            clearTable(primaryStage);
        });

        //Exit program(exit button)
        exitProgramButton.setOnAction(event -> {
            primaryStage.close();
            try{
                lock.release();
            }
            catch(IOException e){
                e.printStackTrace();
            }
        });

        //Exit program(close button)
        primaryStage.setOnCloseRequest(event -> {
            primaryStage.close();
            try{
                lock.release();
            }
            catch(IOException e){
                e.printStackTrace();
            }
        });

        //create new table
        newTable.setOnAction(event -> {
            createTable(primaryStage);
        });


        //delete Table
        deleteTable.setOnAction(event -> {
            deleteTable(primaryStage);
        });


        //shown columns
        editShownColumns.setOnAction(event -> {
            editShownColumns(primaryStage);
        }); 

        HBox bookButtonsHBox = new HBox(addButton, removeButton, moveButton, editButton); 
        HBox listButtonsHBox = new HBox(newTable, deleteTable, clearButton, editShownColumns);
        Pane pane = new Pane(primaryMenuButton, primaryTable, bookButtonsHBox, listButtonsHBox, exitProgramButton);
        Scene scene = new Scene(pane);
        primaryStage.setTitle("The Reading Archive");
        primaryStage.setScene(scene); 
        primaryStage.setMaximized(true);
        primaryStage.show();
        primaryTable.setMinHeight(pane.getHeight() - primaryMenuButton.getHeight() - addButton.getHeight());
        primaryTable.setMinWidth(pane.getWidth());
        primaryTable.setLayoutY(primaryMenuButton.getHeight());
        primaryMenuButton.setLayoutX(pane.getWidth()/2 - primaryMenuButton.getWidth()/2);

        double temp = listButtonsHBox.getWidth() + 50;
        bookButtonsHBox.setLayoutX(listButtonsHBox.getWidth() + 50);
        exitProgramButton.setLayoutX(temp + bookButtonsHBox.getWidth() + 50);

        double buttonHeight = pane.getHeight() - addButton.getHeight();
        bookButtonsHBox.setLayoutY(buttonHeight);
        listButtonsHBox.setLayoutY(buttonHeight);
        exitProgramButton.setLayoutY(buttonHeight);
        } 
    
    public static void main(String[] args) {
        Application.launch(args);
    }
    

}