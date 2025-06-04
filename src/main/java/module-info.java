module flappybird.demo {
    requires javafx.controls;
    requires javafx.fxml;


    opens flappybird.demo to javafx.fxml;
    exports flappybird.demo;
}