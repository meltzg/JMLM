module JMLM.main {
    requires com.google.common;
    requires gson;
    requires org.apache.commons.codec;
    requires org.apache.commons.io;
    requires jaudiotagger;
    requires javafx.graphics;
    requires javafx.fxml;

    exports org.meltzg.jmlm.client;
}