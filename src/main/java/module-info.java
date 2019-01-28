module JMLM.main {
    requires com.google.common;
    requires gson;
    requires org.apache.commons.codec;
    requires org.apache.commons.io;
    requires jaudiotagger;
    requires javafx.graphics;
    requires javafx.fxml;
    requires javafx.controls;
    requires java.sql;

    exports org.meltzg.jmlm.client;
    exports org.meltzg.jmlm.sync;

    opens org.meltzg.jmlm.client;
}