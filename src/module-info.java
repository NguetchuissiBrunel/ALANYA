module ALANYA {
    requires java.sql;
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.swing;
    requires org.slf4j;
    requires emoji.java;
    requires webcam.capture;


    opens sac;
    opens sac.Interface;
    opens sac.client;
    opens sac.BD;
    opens sac.mecanisme;
}