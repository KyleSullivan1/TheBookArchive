package TheBookArchive;

public class Book {
    private String title;
    private String author;
    private String yearPublished;
    private String numPages;
    private String isbn;
    private String startDate;
    private String endDate;
    private String rating;
    private String imagePath;
    //private String imagePath; //may get rid of. Store image path in database then use path to store image in javafx


    Book(String title, String author, String yearPublished, String numPages, String isbn, String startDate, String endDate, String rating, String imagePath) {
        this.title = title;
        this.author = author;
        this.yearPublished = yearPublished;
        this.numPages = numPages;
        this.isbn = isbn;
        this.startDate = startDate;
        this.endDate = endDate;
        this.rating = rating;
        this.imagePath = imagePath;
    }

    public String getTitle(){
        return title;
    }

    public String getAuthor(){
        return author;
    }

    public String getYearPublished(){
        return yearPublished;
    }

    public String getIsbn(){
        return isbn;
    }

    public String getNumPages(){
        return numPages;
    }

    public String getStartDate(){
        return startDate;
    }

    public String getEndDate(){
        return endDate;
    }

    public String getRating(){
        return rating;
    }

    public String getImagePath(){
        return imagePath;
    }
    
}
