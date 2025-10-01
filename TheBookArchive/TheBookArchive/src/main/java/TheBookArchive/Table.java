package TheBookArchive;

public class Table {
    private String name; //table name
    private String nameInDB;

    public Table(String name){
        this.name = name;
        nameInDB = name.replace(" ", "");
    }

    public void setName(String name){
        this.name = name;
    }
    public String getName(){
        return name;
    }

    public void setNameInDB(String nameInDB){
        this.nameInDB = nameInDB;
    }
    public String getNameInDB(){
        return nameInDB;
    }

}
