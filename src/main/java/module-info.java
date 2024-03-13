module com.example.puzzlejfx {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;


    opens com.example.puzzlejfx to javafx.fxml;
    exports com.example.puzzlejfx;
}