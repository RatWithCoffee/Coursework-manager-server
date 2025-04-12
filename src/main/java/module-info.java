module coursework.manager.server.main {
    requires com.fasterxml.jackson.databind;
    requires org.postgresql.jdbc;
    requires java.rmi;
    requires java.sql;
    requires jdk.httpserver;
    requires static lombok;
    exports coursework_manager.rmi_interfaces to java.rmi;
}